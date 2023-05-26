-- public.project definition

-- Drop table

-- DROP TABLE public.project;

CREATE TABLE public.project (
	id bigserial NOT NULL,
	created_at timestamp(6) NULL,
	deleted_at timestamp(6) NULL,
	deskripsi varchar(255) NULL,
	is_active bool NULL,
	is_deleted bool NOT NULL,
	kode varchar(255) NOT NULL,
	nama varchar(255) NOT NULL,
	public_id uuid NOT NULL,
	updated_at timestamp(6) NULL,
	CONSTRAINT project_pkey PRIMARY KEY (id),
	CONSTRAINT uk_rjh8v3rfg1p5sgpnfes7xcm16 UNIQUE (kode)
);


-- public.modul definition

-- Drop table

-- DROP TABLE public.modul;

CREATE TABLE public.modul (
	id bigserial NOT NULL,
	created_at timestamp(6) NULL,
	deleted_at timestamp(6) NULL,
	deskripsi text NULL,
	is_active bool NULL,
	is_deleted bool NOT NULL,
	is_private bool NULL,
	kode varchar(255) NOT NULL,
	nama varchar(255) NULL,
	public_id uuid NOT NULL,
	updated_at timestamp(6) NULL,
	project_id int8 NOT NULL,
	CONSTRAINT modul_pkey PRIMARY KEY (id),
	CONSTRAINT uk_9ugc4rl9teo7gkw97mr3oo50s UNIQUE (kode),
	CONSTRAINT fkfdbbk07q6oeu3s24y0kvd8fyq FOREIGN KEY (project_id) REFERENCES public.project(id)
);


-- public.pengguna definition

-- Drop table

-- DROP TABLE public.pengguna;

CREATE TABLE public.pengguna (
	id bigserial NOT NULL,
	created_at timestamp(6) NULL,
	deleted_at timestamp(6) NULL,
	is_deleted bool NOT NULL,
	is_locked bool NULL,
	nama varchar(255) NULL,
	"password" varchar(255) NULL,
	password_kedaluwarsa timestamp(6) NULL,
	public_id uuid NOT NULL,
	updated_at timestamp(6) NULL,
	username varchar(255) NULL,
	created_by int8 NULL,
	deleted_by int8 NULL,
	updated_by int8 NULL,
	auth_type varchar(255),
	CONSTRAINT pengguna_pkey PRIMARY KEY (id),
	CONSTRAINT uk_58qkm9mhgl2dp72xniogakhxf UNIQUE (username),
	CONSTRAINT uk_akwk7outey2jcj5yyq1ic8kw9 UNIQUE (public_id),
	CONSTRAINT fk62harhthts6gx6ixiomk1quk1 FOREIGN KEY (updated_by) REFERENCES public.pengguna(id),
	CONSTRAINT fkgpduaarwgqj0y3ey2gvniwsew FOREIGN KEY (deleted_by) REFERENCES public.pengguna(id),
	CONSTRAINT fkmnluo78b632okodxw6isk69ra FOREIGN KEY (created_by) REFERENCES public.pengguna(id)
);


-- public.peran definition

-- Drop table

-- DROP TABLE public.peran;

CREATE TABLE public.peran (
	id bigserial NOT NULL,
	created_at timestamp(6) NULL,
	deleted_at timestamp(6) NULL,
	is_active bool NOT NULL,
	is_deleted bool NOT NULL,
	deskripsi varchar(255) NULL,
	kode varchar(255) NULL,
	nama varchar(255) NULL,
	public_id uuid NOT NULL,
	updated_at timestamp(6) NULL,
	created_by int8 NULL,
	deleted_by int8 NULL,
	updated_by int8 NULL,
	CONSTRAINT peran_pkey PRIMARY KEY (id),
	CONSTRAINT uk_n7dqb0666klk7pgq70523bug3 UNIQUE (public_id),
	CONSTRAINT fk9uwq5qfgfxac0h2f57y5n86nb FOREIGN KEY (deleted_by) REFERENCES public.pengguna(id),
	CONSTRAINT fkcbirdu3rgpmsf9m5eire81kdg FOREIGN KEY (updated_by) REFERENCES public.pengguna(id),
	CONSTRAINT fksdvt7wdn8gi2g7nqnsuvn7mt4 FOREIGN KEY (created_by) REFERENCES public.pengguna(id)
);


-- public.hak_akses definition

-- Drop table

-- DROP TABLE public.hak_akses;

CREATE TABLE public.hak_akses (
	id bigserial NOT NULL,
	created_at timestamp(6) NULL,
	deleted_at timestamp(6) NULL,
	deskripsi text NULL,
	is_active bool NULL,
	is_deleted bool NOT NULL,
	kode varchar(255) NULL,
	nama varchar(255) NULL,
	public_id uuid NOT NULL,
	updated_at timestamp(6) NULL,
	modul_id int8 NOT NULL,
	CONSTRAINT hak_akses_pkey PRIMARY KEY (id),
	CONSTRAINT fk73tq703e8kkjn4hskksc9ujl3 FOREIGN KEY (modul_id) REFERENCES public.modul(id)
);


-- public.pengguna_peran definition

-- Drop table

-- DROP TABLE public.pengguna_peran;

CREATE TABLE public.pengguna_peran (
	pengguna_id int8 NOT NULL,
	peran_id int8 NOT NULL,
	CONSTRAINT pengguna_peran_pkey PRIMARY KEY (pengguna_id, peran_id),
	CONSTRAINT fkauhdbdm93o7f0b3pgplqnd8qq FOREIGN KEY (peran_id) REFERENCES public.peran(id),
	CONSTRAINT fkm2ifyulgn4m72mob0evixj30f FOREIGN KEY (pengguna_id) REFERENCES public.pengguna(id)
);


-- public.peran_hak_akses definition

-- Drop table

-- DROP TABLE public.peran_hak_akses;

CREATE TABLE public.peran_hak_akses (
	peran_id int8 NOT NULL,
	hak_akses_id int8 NOT NULL,
	CONSTRAINT peran_hak_akses_pkey PRIMARY KEY (peran_id, hak_akses_id),
	CONSTRAINT fk2xm5audp2fp48fx7o15dqyogc FOREIGN KEY (peran_id) REFERENCES public.peran(id),
	CONSTRAINT fkiqf5f8enxop44q0io4hdmv46e FOREIGN KEY (hak_akses_id) REFERENCES public.hak_akses(id)
);