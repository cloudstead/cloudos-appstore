--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: app_footprint; Type: TABLE; Schema: public; Owner: appstore; Tablespace: 
--

CREATE TABLE app_footprint (
    uuid character varying(100) NOT NULL,
    ctime bigint NOT NULL,
    cloud_app character varying(100) NOT NULL,
    cpus integer NOT NULL,
    disk_io character varying(255) NOT NULL,
    memory integer NOT NULL,
    network_io character varying(255) NOT NULL
);


ALTER TABLE public.app_footprint OWNER TO appstore;

--
-- Name: app_price; Type: TABLE; Schema: public; Owner: appstore; Tablespace: 
--

CREATE TABLE app_price (
    uuid character varying(100) NOT NULL,
    ctime bigint NOT NULL,
    cloud_app character varying(100) NOT NULL,
    initial_cost integer NOT NULL,
    iso_currency character varying(3),
    monthly_fixed_cost integer NOT NULL,
    payment_required boolean NOT NULL
);


ALTER TABLE public.app_price OWNER TO appstore;

--
-- Name: app_store_account; Type: TABLE; Schema: public; Owner: appstore; Tablespace: 
--

CREATE TABLE app_store_account (
    uuid character varying(100) NOT NULL,
    ctime bigint NOT NULL,
    admin boolean NOT NULL,
    consumer_tos integer,
    email character varying(255) NOT NULL,
    hashed_password character varying(200) NOT NULL,
    reset_token character varying(30),
    reset_token_ctime bigint,
    publisher_tos integer
);


ALTER TABLE public.app_store_account OWNER TO appstore;

--
-- Name: app_store_publisher; Type: TABLE; Schema: public; Owner: appstore; Tablespace: 
--

CREATE TABLE app_store_publisher (
    uuid character varying(100) NOT NULL,
    ctime bigint NOT NULL,
    name character varying(255) NOT NULL,
    owner character varying(100) NOT NULL
);


ALTER TABLE public.app_store_publisher OWNER TO appstore;

--
-- Name: app_store_publisher_member; Type: TABLE; Schema: public; Owner: appstore; Tablespace: 
--

CREATE TABLE app_store_publisher_member (
    uuid character varying(100) NOT NULL,
    ctime bigint NOT NULL,
    account character varying(100) NOT NULL,
    publisher character varying(100) NOT NULL
);


ALTER TABLE public.app_store_publisher_member OWNER TO appstore;

--
-- Name: cloud_app; Type: TABLE; Schema: public; Owner: appstore; Tablespace: 
--

CREATE TABLE cloud_app (
    uuid character varying(100) NOT NULL,
    ctime bigint NOT NULL,
    active_version character varying(100),
    author character varying(100) NOT NULL,
    name character varying(255) NOT NULL,
    publisher character varying(100) NOT NULL
);


ALTER TABLE public.cloud_app OWNER TO appstore;

--
-- Name: cloud_app_client; Type: TABLE; Schema: public; Owner: appstore; Tablespace: 
--

CREATE TABLE cloud_app_client (
    uuid character varying(100) NOT NULL,
    ctime bigint NOT NULL,
    client_url character varying(1024) NOT NULL,
    client_url_sha character varying(200) NOT NULL,
    cloud_app character varying(100) NOT NULL,
    type character varying(255)
);


ALTER TABLE public.cloud_app_client OWNER TO appstore;

--
-- Name: cloud_app_version; Type: TABLE; Schema: public; Owner: appstore; Tablespace: 
--

CREATE TABLE cloud_app_version (
    uuid character varying(100) NOT NULL,
    ctime bigint NOT NULL,
    app character varying(100) NOT NULL,
    author character varying(100) NOT NULL,
    description character varying(16384) NOT NULL,
    large_icon_url character varying(1024) NOT NULL,
    large_icon_url_sha character varying(200) NOT NULL,
    metadata character varying(65536),
    small_icon_url character varying(1024) NOT NULL,
    small_icon_url_sha character varying(200) NOT NULL,
    previous_version character varying(100),
    server_config_url character varying(1024) NOT NULL,
    server_config_url_sha character varying(200) NOT NULL,
    status character varying(255) NOT NULL,
    major character varying(30),
    minor character varying(30),
    patch character varying(30)
);


ALTER TABLE public.cloud_app_version OWNER TO appstore;

--
-- Name: published_app; Type: TABLE; Schema: public; Owner: appstore; Tablespace: 
--

CREATE TABLE published_app (
    uuid character varying(100) NOT NULL,
    ctime bigint NOT NULL,
    app character varying(100) NOT NULL,
    author character varying(100) NOT NULL,
    description character varying(16384) NOT NULL,
    large_icon_url character varying(1024) NOT NULL,
    large_icon_url_sha character varying(200) NOT NULL,
    metadata character varying(65536),
    small_icon_url character varying(1024) NOT NULL,
    small_icon_url_sha character varying(200) NOT NULL,
    previous_version character varying(100),
    server_config_url character varying(1024) NOT NULL,
    server_config_url_sha character varying(200) NOT NULL,
    status character varying(255) NOT NULL,
    major character varying(30),
    minor character varying(30),
    patch character varying(30),
    approved_by character varying(100) NOT NULL
);


ALTER TABLE public.published_app OWNER TO appstore;

--
-- Data for Name: app_footprint; Type: TABLE DATA; Schema: public; Owner: appstore
--

COPY app_footprint (uuid, ctime, cloud_app, cpus, disk_io, memory, network_io) FROM stdin;
\.


--
-- Data for Name: app_price; Type: TABLE DATA; Schema: public; Owner: appstore
--

COPY app_price (uuid, ctime, cloud_app, initial_cost, iso_currency, monthly_fixed_cost, payment_required) FROM stdin;
07f5d20d-aa69-4db0-900b-b5027f3b815e	1393624672769	ecff1ec9-b29b-4b9d-abdd-def10c1502e6	1000	USD	100	f
\.


--
-- Data for Name: app_store_account; Type: TABLE DATA; Schema: public; Owner: appstore
--

COPY app_store_account (uuid, ctime, admin, consumer_tos, email, hashed_password, reset_token, reset_token_ctime, publisher_tos) FROM stdin;
b748897f-db7a-4344-b263-ee96db458a70	1393624672370	t	\N	wMTYz1393624672048@example.com	$2a$04$A5Mj7f0TIvaosuptoTFf.OQCjDCO.qWCC1.hiTOk7BDmdnhddvOe.	\N	\N	1
62caf051-2055-4ee6-a85f-e91fd7edbee1	1393624672549	f	\N	hYidB1393624672529@example.com	$2a$04$zirDWZGvtxhCAptX3uN2pedk1SN2lheDbG.CsSIxDlAFe5zbErvr6	\N	\N	1
\.


--
-- Data for Name: app_store_publisher; Type: TABLE DATA; Schema: public; Owner: appstore
--

COPY app_store_publisher (uuid, ctime, name, owner) FROM stdin;
b748897f-db7a-4344-b263-ee96db458a70	1393624672428	tIJWkPX06sxoAKwY6MF7	b748897f-db7a-4344-b263-ee96db458a70
62caf051-2055-4ee6-a85f-e91fd7edbee1	1393624672565	mk8epX26xoNza4ZinFBc	62caf051-2055-4ee6-a85f-e91fd7edbee1
\.


--
-- Data for Name: app_store_publisher_member; Type: TABLE DATA; Schema: public; Owner: appstore
--

COPY app_store_publisher_member (uuid, ctime, account, publisher) FROM stdin;
2b6af507-ce31-43fb-9bab-9459d9aae699	1393624672437	b748897f-db7a-4344-b263-ee96db458a70	b748897f-db7a-4344-b263-ee96db458a70
a74ea2c8-4464-4692-a44f-92bdfa62fb62	1393624672572	62caf051-2055-4ee6-a85f-e91fd7edbee1	62caf051-2055-4ee6-a85f-e91fd7edbee1
\.


--
-- Data for Name: cloud_app; Type: TABLE DATA; Schema: public; Owner: appstore
--

COPY cloud_app (uuid, ctime, active_version, author, name, publisher) FROM stdin;
ecff1ec9-b29b-4b9d-abdd-def10c1502e6	1393624672611	86d8ab05-dc35-4ef3-a54a-683f62e99ff5	62caf051-2055-4ee6-a85f-e91fd7edbee1	CloudFiles	62caf051-2055-4ee6-a85f-e91fd7edbee1
\.


--
-- Data for Name: cloud_app_client; Type: TABLE DATA; Schema: public; Owner: appstore
--

COPY cloud_app_client (uuid, ctime, client_url, client_url_sha, cloud_app, type) FROM stdin;
\.


--
-- Data for Name: cloud_app_version; Type: TABLE DATA; Schema: public; Owner: appstore
--

COPY cloud_app_version (uuid, ctime, app, author, description, large_icon_url, large_icon_url_sha, metadata, small_icon_url, small_icon_url_sha, previous_version, server_config_url, server_config_url_sha, status, major, minor, patch) FROM stdin;
86d8ab05-dc35-4ef3-a54a-683f62e99ff5	1393624672693	ecff1ec9-b29b-4b9d-abdd-def10c1502e6	b748897f-db7a-4344-b263-ee96db458a70	Store your files in the cloud	file:///var/folders/lf/m6vk2nk56jb74bd5v1_9knbm0000gn/T/stream2file1722406449290197985.tmp	49eb4956f822130ddc85b80c4d07fb09caad4851b3fb8e790600f8df8992b31a	\N	file:///var/folders/lf/m6vk2nk56jb74bd5v1_9knbm0000gn/T/stream2file2003047014267419938.tmp	8e8d758fd68cdc8724dffd98b40d04962dfb6087722af04650035ae4aa6c47e8	\N	file:///var/folders/lf/m6vk2nk56jb74bd5v1_9knbm0000gn/T/stream2file7038486973763476225.tmp	a777a3e70451ec463132cef947ab4f8df22563ff4d1448594441e55ae8ec43c4	PUBLISHED	1	0	0
\.


--
-- Data for Name: published_app; Type: TABLE DATA; Schema: public; Owner: appstore
--

COPY published_app (uuid, ctime, app, author, description, large_icon_url, large_icon_url_sha, metadata, small_icon_url, small_icon_url_sha, previous_version, server_config_url, server_config_url_sha, status, major, minor, patch, approved_by) FROM stdin;
ecff1ec9-b29b-4b9d-abdd-def10c1502e6	1393624672858	ecff1ec9-b29b-4b9d-abdd-def10c1502e6	62caf051-2055-4ee6-a85f-e91fd7edbee1	Store your files in the cloud	file:///var/folders/lf/m6vk2nk56jb74bd5v1_9knbm0000gn/T/stream2file1722406449290197985.tmp	49eb4956f822130ddc85b80c4d07fb09caad4851b3fb8e790600f8df8992b31a	\N	file:///var/folders/lf/m6vk2nk56jb74bd5v1_9knbm0000gn/T/stream2file2003047014267419938.tmp	8e8d758fd68cdc8724dffd98b40d04962dfb6087722af04650035ae4aa6c47e8	\N	file:///var/folders/lf/m6vk2nk56jb74bd5v1_9knbm0000gn/T/stream2file7038486973763476225.tmp	a777a3e70451ec463132cef947ab4f8df22563ff4d1448594441e55ae8ec43c4	NEW	1	0	0	b748897f-db7a-4344-b263-ee96db458a70
\.


--
-- Name: app_footprint_cloud_app_key; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY app_footprint
    ADD CONSTRAINT app_footprint_cloud_app_key UNIQUE (cloud_app);


--
-- Name: app_footprint_pkey; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY app_footprint
    ADD CONSTRAINT app_footprint_pkey PRIMARY KEY (uuid);


--
-- Name: app_price_cloud_app_iso_currency_key; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY app_price
    ADD CONSTRAINT app_price_cloud_app_iso_currency_key UNIQUE (cloud_app, iso_currency);


--
-- Name: app_price_pkey; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY app_price
    ADD CONSTRAINT app_price_pkey PRIMARY KEY (uuid);


--
-- Name: app_store_account_email_key; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY app_store_account
    ADD CONSTRAINT app_store_account_email_key UNIQUE (email);


--
-- Name: app_store_account_pkey; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY app_store_account
    ADD CONSTRAINT app_store_account_pkey PRIMARY KEY (uuid);


--
-- Name: app_store_publisher_member_account_key; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY app_store_publisher_member
    ADD CONSTRAINT app_store_publisher_member_account_key UNIQUE (account);


--
-- Name: app_store_publisher_member_pkey; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY app_store_publisher_member
    ADD CONSTRAINT app_store_publisher_member_pkey PRIMARY KEY (uuid);


--
-- Name: app_store_publisher_member_publisher_account_key; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY app_store_publisher_member
    ADD CONSTRAINT app_store_publisher_member_publisher_account_key UNIQUE (publisher, account);


--
-- Name: app_store_publisher_member_publisher_key; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY app_store_publisher_member
    ADD CONSTRAINT app_store_publisher_member_publisher_key UNIQUE (publisher);


--
-- Name: app_store_publisher_name_key; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY app_store_publisher
    ADD CONSTRAINT app_store_publisher_name_key UNIQUE (name);


--
-- Name: app_store_publisher_owner_key; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY app_store_publisher
    ADD CONSTRAINT app_store_publisher_owner_key UNIQUE (owner);


--
-- Name: app_store_publisher_pkey; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY app_store_publisher
    ADD CONSTRAINT app_store_publisher_pkey PRIMARY KEY (uuid);


--
-- Name: cloud_app_client_cloud_app_key; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY cloud_app_client
    ADD CONSTRAINT cloud_app_client_cloud_app_key UNIQUE (cloud_app);


--
-- Name: cloud_app_client_pkey; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY cloud_app_client
    ADD CONSTRAINT cloud_app_client_pkey PRIMARY KEY (uuid);


--
-- Name: cloud_app_name_key; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY cloud_app
    ADD CONSTRAINT cloud_app_name_key UNIQUE (name);


--
-- Name: cloud_app_pkey; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY cloud_app
    ADD CONSTRAINT cloud_app_pkey PRIMARY KEY (uuid);


--
-- Name: cloud_app_version_pkey; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY cloud_app_version
    ADD CONSTRAINT cloud_app_version_pkey PRIMARY KEY (uuid);


--
-- Name: published_app_pkey; Type: CONSTRAINT; Schema: public; Owner: appstore; Tablespace: 
--

ALTER TABLE ONLY published_app
    ADD CONSTRAINT published_app_pkey PRIMARY KEY (uuid);


--
-- Name: public; Type: ACL; Schema: -; Owner: jcobb
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM jcobb;
GRANT ALL ON SCHEMA public TO jcobb;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

