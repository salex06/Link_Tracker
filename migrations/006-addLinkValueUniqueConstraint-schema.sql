ALTER TABLE link
ADD CONSTRAINT link_value_must_be_unique
UNIQUE (link_value);
