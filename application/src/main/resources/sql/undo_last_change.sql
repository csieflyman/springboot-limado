
-- START UNDO OF CHANGE SCRIPT #3: 003_create_intervaltree_table.sql

START TRANSACTION;



DELETE FROM changelog WHERE change_number = 3;

COMMIT;

-- END UNDO OF CHANGE SCRIPT #3: 003_create_intervaltree_table.sql


-- START UNDO OF CHANGE SCRIPT #2: 002_create_dagedge_table.sql

START TRANSACTION;



DELETE FROM changelog WHERE change_number = 2;

COMMIT;

-- END UNDO OF CHANGE SCRIPT #2: 002_create_dagedge_table.sql


-- START UNDO OF CHANGE SCRIPT #1: 001_create_party_table.sql

START TRANSACTION;



DELETE FROM changelog WHERE change_number = 1;

COMMIT;

-- END UNDO OF CHANGE SCRIPT #1: 001_create_party_table.sql

