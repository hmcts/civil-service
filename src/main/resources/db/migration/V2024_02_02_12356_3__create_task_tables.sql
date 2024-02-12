CREATE SEQUENCE IF NOT EXISTS dbs.task_item_template_id_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

CREATE TABLE IF NOT EXISTS dbs.task_item_template (
                                                       id BIGINT PRIMARY KEY DEFAULT nextval('dbs.task_item_template_id_seq'),
                                                       task_name_en         VARCHAR(256),
                                                       hint_text_en         VARCHAR(512),
                                                       task_name_cy         VARCHAR(256),
                                                       hint_text_cy         VARCHAR(512),
                                                       name character varying(256),
                                                       task_status_sequence int[],
                                                       role character varying(256),
                                                       task_order int,
                                                       created_at timestamp without time zone DEFAULT now() NOT NULL
);



CREATE TABLE IF NOT EXISTS dbs.task_list (
                                              id uuid NOT NULL PRIMARY KEY,
                                              task_item_template_id bigint NOT NULL,
                                              reference character varying(256),
                                              current_status int,
                                              next_status int,
                                              category_en           VARCHAR(256),
                                              category_cy           VARCHAR(256),
                                              message_parm jsonb,
                                              created_at timestamp without time zone DEFAULT now() NOT NULL,
                                              updated_at timestamp without time zone,
                                              updated_by character varying(256),
                                              CONSTRAINT fk_task_list_task_item_template FOREIGN KEY (task_item_template_id) REFERENCES dbs.task_item_template(id)
);


