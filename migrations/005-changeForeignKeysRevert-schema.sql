ALTER TABLE tg_chat_link DROP CONSTRAINT IF EXISTS tg_chat_link_tg_chat_id_fkey;

ALTER TABLE tg_chat_link
ADD CONSTRAINT tg_chat_link_tg_chat_id_fkey
FOREIGN KEY (tg_chat_id)
REFERENCES tg_chat(id)
ON DELETE CASCADE;

ALTER TABLE chat_link_tags DROP CONSTRAINT IF EXISTS link_tags_chat_id_fkey;

ALTER TABLE chat_link_tags
ADD CONSTRAINT link_tags_chat_id_fkey
FOREIGN KEY (chat_id)
REFERENCES tg_chat(id)
ON DELETE CASCADE;

ALTER TABLE chat_link_filters DROP CONSTRAINT IF EXISTS link_filters_chat_id_fkey;

ALTER TABLE chat_link_filters
ADD CONSTRAINT link_filters_chat_id_fkey
FOREIGN KEY (chat_id)
REFERENCES tg_chat(id)
ON DELETE CASCADE;
