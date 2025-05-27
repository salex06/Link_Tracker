ALTER TABLE link
ADD COLUMN type varchar(30) check (type IN ('github', 'stackoverflow', 'undefined')) DEFAULT 'undefined'
