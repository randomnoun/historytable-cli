package com.randomnoun.common.db.history;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import javax.script.ScriptException;
import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.randomnoun.common.StreamUtil;
import com.randomnoun.common.db.SqlParser;
import com.randomnoun.common.db.SqlParser.Consumer;
import com.randomnoun.common.log4j.Log4jCliConfiguration;

/** CLI for the history table generator
 * 
 **/
public class HistoryTableGeneratorCli {

	Logger logger = Logger.getLogger(HistoryTableGeneratorCli.class);
		
	private DataSource ds;
	
	public HistoryTableGeneratorCli(DataSource ds) {
		this.ds = ds;
	}
	
	public void run(String script, final boolean execute, final boolean quiet, Properties props) throws UnsupportedEncodingException, IOException, ScriptException, java.text.ParseException, SQLException {
		String jessop;
		if (script == null) {
			script = "<internal>";
			InputStream is = HistoryTableGeneratorCli.class.getResourceAsStream("/common/db/mysql/mysql-historyTable-2.sql.jessop");
			jessop = new String(StreamUtil.getByteArray(is), "utf-8");
			
		} else {
			File f = new File(script);
			InputStream is = new FileInputStream(f);
			jessop = new String(StreamUtil.getByteArray(is), "utf-8");
			is.close();
		}
		
		HistoryTableGenerator htg = new HistoryTableGenerator(ds);
		htg.setJessopScript(jessop);
		htg.setJessopScriptFilename(script);
		htg.setSchemaName("datatype-dev");
		Map<String, Object> htgOptions = htg.getOptions();		
		for (Map.Entry<Object, Object> e : props.entrySet()) {
			htgOptions.put((String) e.getKey(), (String) e.getValue());
		}
		htg.setOptions(htgOptions);
		String sql = htg.generateHistoryTableSql();
		
		final Connection connection = ds.getConnection();
		if (!(execute || quiet)) {
			// dump the sql to stdout
			System.out.println(sql);
		} else {
			SqlParser sp = new SqlParser();
			Reader r = new StringReader(sql);
			sp.consumeStatements(r,  true, new Consumer<String>() {
				@Override
				public void consume(String s) {
					if (!quiet) {
						System.out.println(s);
					}
					if (execute) {
						try {
							Statement st = connection.createStatement();
							st.execute(s);
							st.close();
						} catch (SQLException sqle) {
							throw new RuntimeException("SQL exception", sqle);
						}
					}
					
				}
			}); 
		}
		connection.close();
		
	}
	
	public static void main(String args[]) throws ScriptException, SQLException, ClassNotFoundException, IOException, java.text.ParseException {
		// create the command line parser
		CommandLineParser parser = new DefaultParser();

		// create the Options
		Options options = new Options();
		options.addOption( Option.builder("h").longOpt( "help" ).desc( "This usage text" ).build() );
		options.addOption( Option.builder("j").longOpt( "jdbc" ).desc( "JDBC connection string" ).required().hasArg().argName("jdbc").build() );
		options.addOption( Option.builder("u").longOpt( "username" ).desc( "JDBC username" ).hasArg().argName("username").build() );
		options.addOption( Option.builder("p").longOpt( "password" ).desc( "JDBC password" ).hasArg().argName("password").build() );
		options.addOption( Option.builder("d").longOpt( "driver" ).desc( "JDBC driver class name; default = org.mariadb.jdbc.Driver" ).hasArg().argName("driver").build() );
		options.addOption( Option.builder("s").longOpt( "script" ).desc( "Script filename" ).hasArg().argName("script").build() );
		options.addOption( Option.builder("x").longOpt( "execute" ).desc( "Execute generated SQL" ).build() );
		options.addOption( Option.builder("q").longOpt( "quiet" ).desc( "Quiet mode" ).build() );

		options.addOption( Option.builder("O").longOpt( "option" ).desc( "use value for given script option" )
            .hasArgs().valueSeparator('=').argName("property=value").build() );

		// maybe get these from the script itself. although, why ?
		String footer = "\n" +
		  "The options available will depend on the script chosen.\n" +
		  "\n" +
		  "The default script operates on mysql databases and handles the following options:\n" +
		  "\n" +
		  "  undoEnabledTableNames - (csv list) names of tables that can be undone\n" +
		  "\n" +
		  "  dropTables - (boolean) true to drop and recreate history SQL tables\n" +
		  "      false to alter existing tables\n" +
		  "\n" +
		  "  existingDataUserActionId - (long) the ID to use for existing data when populating initial history\n" +
		  "      table records\n" +
		  "\n" +
		  "  alwaysRecreateTriggers - (boolean) when true, drop and recreate triggers even if table\n" +
		  "      is unchanged\n" +
		  "\n" +
		  "  alwaysRecreateStoredProcedures - (boolean) when true, drop and recreate stored procedures\n" +
		  "      even if table is unchanged\n" +
		  "\n" +
		  "  includeUpdateBitFields - (boolean) when true, include an 'update' bit field for each column\n" +
		  "      in the history table\n" +
		  "\n" +
		  "  includeCurrentUser - (boolean) when true, include a current user column in the history table\n";
				
		CommandLine line = null;
		try {
		    line = parser.parse( options, args );
		} catch (ParseException exp) {
		    System.err.println( exp.getMessage() );
		    HelpFormatter formatter = new HelpFormatter();
		    formatter.setWidth(100);
		    formatter.setOptionComparator(null);
		    formatter.printHelp( "HistoryTableGeneratorCli [options]", null, options, footer );
		    System.exit(1);
		}	

		// String driverName = line.getOptionValue("driverName", "com.mysql.jdbc.Driver");
		String driverName = line.getOptionValue("driverName", "org.mariadb.jdbc.Driver");
		String jdbc = line.getOptionValue("jdbc");
		String username = line.getOptionValue("username");
		String password = line.getOptionValue("password");
		String script = line.getOptionValue("script");
		final boolean execute = line.hasOption("execute");
		final boolean quiet = line.hasOption("quiet");
		Properties props = line.getOptionProperties("O");
		
		// @TODO verbose flag
		Log4jCliConfiguration lcc = new Log4jCliConfiguration();
		Properties logProps = new Properties();
		logProps.put("log4j.rootCategory", "WARN, CONSOLE");
		lcc.init("[HistoryTableGeneratorCli]", logProps);
		
		Class.forName(driverName);
		
		Connection conn = DriverManager.getConnection(jdbc, username, password);
		DataSource ds = new SingleConnectionDataSource(conn, false);

		HistoryTableGeneratorCli htgc = new HistoryTableGeneratorCli(ds);
		htgc.run(script, execute, quiet, props);
	}
}
