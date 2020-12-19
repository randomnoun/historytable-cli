
# historytable-cli

**historytable-cli**  generates stored procedures and triggers to convert your plain old RDBMS into a data warehouse &trade;.

Point this thing at a database, run some commands, and then any time a change is made to that database, a record  is written into an append-only history table. 

This will make it possible to revert changes in your database back to an earlier point in time without having to rollback the entire thing from your last backup.

## Why would anyone want to do that  ?

To give your enterprise-ready distributed modular rules engine the same undo functionality that Microsoft Paint had in the early 90s.

It's a CLI, presumably because I was going to write up a blog post about this and wanted to show how easy it was to use. For people who find CLIs easy to use, obviously.

So once I get around to writing that, I'll link to it.

Here it is:

http://www.randomnoun.com/wp/2020/12/19/the-complete-history-of-everything/

## How do I use it ? 

Usage text:
```
C:\util\java> java -jar historytable-cli-0.0.5-with-dependencies.jar
Missing required option: j
usage: HistoryTableGeneratorCli [options]
 -h,--help                      This usage text
 -j,--jdbc <jdbc>               JDBC connection string
 -u,--username <username>       JDBC username
 -p,--password <password>       JDBC password
 -d,--driver <driver>           JDBC driver class name; default = com.mysql.jdbc.Driver
 -s,--script <script>           Script filename
 -x,--execute                   Execute generated SQL
 -q,--quiet                     Quiet mode
 -O,--option <property=value>   use value for given script option

The options available will depend on the script chosen.

The default script handles the following options:

  undoEnabledTableNames - (csv list) names of tables that can be undone

  dropTables - (boolean) true to drop and recreate history SQL tables
      false to alter existing tables

  existingDataUserActionId - (long) the ID to use for existign data when populating initial history
      table records

  alwaysRecreateTriggers - (boolean) when true, drop and recreate triggers even if table
      is unchanged

  alwaysRecreateStoredProcedures - (boolean) when true, drop and recreate stored procedures
      even if table is unchanged

  includeUpdateBitFields - (boolean) when true, include an 'update' bit field for each column
      in the history table

  includeCurrentUser - (boolean) when true, include a current user column in the history table
```

And an example command line:

```
C:\util\java> java -jar historytable-cli-0.0.5-with-dependencies.jar --jdbc jdbc:mysql://localhost/datatype-dev --username root --password abc123
DROP TRIGGER IF EXISTS `trgtblalltypeInsert`;
DROP TRIGGER IF EXISTS `trgtblalltypeUpdate`;
DROP TRIGGER IF EXISTS `trgtblalltypeDelete`;
CREATE TABLE `datatype-dev`.`tblalltypeHistory` (
[... the SQL to create the history tables and triggers ...]
```

## Doesn't this already exist ?

Probably. I wrote this close to a decade ago, based on a system I saw in use 15 years ago, so there's a good chance there's other products out there that do the same sort of thing. 


## Licensing

historytable-cli is licensed under the BSD 2-clause license.

