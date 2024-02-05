CREATE TABLE IF NOT EXISTS public.task_item_template (
                                         id bigint NOT NULL,
                                         title_en character varying(256),
                                         content_en character varying(512),
                                         category_en character varying(256),
                                         title_cy character varying(256),
                                         content_cy character varying(512),
                                         category_cy character varying(256),
                                         name character varying(256),
                                         task_status_sequence integer[],
                                         role character varying(256),
                                         task_order integer,
                                         created_at timestamp without time zone DEFAULT now() NOT NULL,
                                         CONSTRAINT task_list_template_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.task_item_identifier (
                                           id uuid NOT NULL,
                                           task_item_template_id bigint NOT NULL,
                                           case_reference character varying(20) NOT NULL,
                                           CONSTRAINT task_item_identifier_pkey PRIMARY KEY (id),
                                           CONSTRAINT fk_task_item_identifier_task_item_template FOREIGN KEY (task_item_template_id) REFERENCES public.task_item_template(id)
);





CREATE TABLE IF NOT EXISTS public.task_list (
                                id uuid NOT NULL,
                                current_status integer,
                                next_status integer,
                                task_item_en character varying(512),
                                task_item_cy character varying(512),
                                message_parm character varying(256),
                                created_at timestamp without time zone DEFAULT now() NOT NULL,
                                modified_at timestamp without time zone,
                                created_by character varying(256),
                                modified_by character varying(256),
                                CONSTRAINT task_list_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS task_item_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
