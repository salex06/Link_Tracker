ALTER TABLE IF EXISTS link_tags RENAME TO chat_link_tags;

ALTER TABLE chat_link_tags
ADD chat_id BIGINT NOT NULL;

ALTER TABLE chat_link_tags DROP CONSTRAINT link_tags_pkey;

ALTER TABLE chat_link_tags ADD CONSTRAINT link_tags_pkey PRIMARY KEY (chat_id, link_id, tag_value);
