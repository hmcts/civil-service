CREATE SEQUENCE IF NOT EXISTS public.task_item_template_id_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

CREATE TABLE IF NOT EXISTS public.task_item_template (
                                                       id bigint NOT NULL,
                                                       title_en character varying(256),
                                                       content_en character varying(512),
                                                       category_en character varying(256),
                                                       title_cy character varying(256),
                                                       content_cy character varying(512),
                                                       category_cy character varying(256),
                                                       name character varying(256),
                                                       task_status_sequence smallint[],
                                                       role character varying(256),
                                                       task_order smallint,
                                                       created_at timestamp without time zone DEFAULT now() NOT NULL,
                                                       CONSTRAINT task_list_template_pkey PRIMARY KEY (id)
);



CREATE TABLE IF NOT EXISTS public.task_list (
                                              id uuid NOT NULL,
                                              task_item_template_id bigint NOT NULL,
                                              reference character varying(256),
                                              current_status smallint,
                                              next_status smallint,
                                              task_item_en character varying(512),
                                              task_item_cy character varying(512),
                                              message_parm jsonb,
                                              created_at timestamp without time zone DEFAULT now() NOT NULL,
                                              updated_at timestamp without time zone,
                                              updated_by character varying(256),
                                              CONSTRAINT task_list_pkey PRIMARY KEY (id),
                                              CONSTRAINT fk_task_list_task_item_template FOREIGN KEY (task_item_template_id) REFERENCES public.task_item_template(id)
);


