# historytable-cli

**historytable-cli**  generates stored procedures and triggers to convert your plain old RDBMS into a data warehouse (TM).

Point this thing at a database, run some commands, and then any time a change is made to that database, a record  is written into an append-only history table, which you'll find convenient when you need to rollback something and you don't want to rollback the entire database from backup.

## Why would anyone want to do that  ?

To give your enterprise-ready distributed modular rules engine the same undo functionality that Microsoft Paint had in the early 90s.

## How do I use it

It's a CLI, presumably because I was going to write up a blog post about this and wanted to show how easy it was to use. For people who find CLIs easy to use, obviously.

So once I get around to writing that, I'll link to it.

## Licensing

historytable-cli is licensed under the BSD 2-clause license.