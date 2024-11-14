CREATE SEQUENCE IF NOT EXISTS dbs.task_item_template_id_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

CREATE TABLE IF NOT EXISTS dbs.task_item_template (
       id BIGINT PRIMARY KEY DEFAULT nextval('dbs.task_item_template_id_seq'),
       task_name_en         character varying(256),
       hint_text_en         character varying(512),
       category_en          character varying(256),
       task_name_cy         character varying(256),
       hint_text_cy         character varying(512),
       category_cy          character varying(256),
       scenario_name character varying(256),
       template_name character varying(256),
       task_status_sequence int[],
       role character varying(256),
       task_order int,
       created_at TIMESTAMP default CURRENT_TIMESTAMP
);



CREATE TABLE IF NOT EXISTS dbs.task_list (
      id uuid NOT NULL PRIMARY KEY,
      task_item_template_id bigint NOT NULL,
      reference character varying(256),
      current_status int,
      next_status int,
      task_name_en  character varying(256),
      hint_text_en  character varying(4096),
      task_name_cy  character varying(256),
      hint_text_cy  character varying(4096),
      message_params jsonb,
      created_at TIMESTAMP default CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      updated_by character varying(256),
      CONSTRAINT fk_task_list_task_item_template
      FOREIGN KEY (task_item_template_id) REFERENCES dbs.task_item_template(id)
);


