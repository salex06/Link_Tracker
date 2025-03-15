ALTER TABLE IF EXISTS link_filters RENAME TO chat_link_filters;

ALTER TABLE chat_link_filters
ADD chat_id BIGINT NOT NULL;

ALTER TABLE chat_link_filters DROP CONSTRAINT link_filters_pkey;

ALTER TABLE chat_link_filters ADD CONSTRAINT link_filters_pkey PRIMARY KEY (chat_id, link_id, filter_value);
