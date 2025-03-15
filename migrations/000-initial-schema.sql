CREATE TABLE IF NOT EXISTS tg_chat(
    id BIGSERIAL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS link(
    id BIGSERIAL PRIMARY KEY,
    link_value VARCHAR(255) NOT NULL,
    last_update TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tg_chat_link(
    tg_chat_id BIGINT REFERENCES tg_chat(id) ON DELETE CASCADE,
    link_id BIGINT REFERENCES link(id) ON DELETE CASCADE,
    PRIMARY KEY(tg_chat_id, link_id)
);

CREATE TABLE IF NOT EXISTS link_tags(
    link_id BIGINT REFERENCES link(id) ON DELETE CASCADE,
    tag_value TEXT NOT NULL,
    PRIMARY KEY (link_id, tag_value)
);

CREATE TABLE IF NOT EXISTS link_filters(
    link_id BIGINT REFERENCES link(id) ON DELETE CASCADE,
    filter_value TEXT NOT NULL,
    PRIMARY KEY (link_id, filter_value)
);
