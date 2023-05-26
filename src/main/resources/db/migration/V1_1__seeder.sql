INSERT INTO public.project (created_at,deleted_at,deskripsi,is_active,is_deleted,kode,nama,public_id,updated_at) VALUES
	 ('2022-08-23 21:46:39.455',NULL,'Project Autentikasi',true,false,'AU','Autentikasi','f5969858-a193-4781-8176-80977da2701d','2022-08-23 21:46:39.455'),
	 ('2023-01-01 12:39:44.285',NULL,'Project Akuntansi',true,false,'AK','Akuntansi','eaccb176-b918-41a0-8f2b-fb588140385d','2023-01-01 12:39:44.285');

INSERT INTO public.modul (created_at,deleted_at,deskripsi,is_active,is_deleted,is_private,kode,nama,public_id,updated_at,project_id) VALUES
	 ('2022-08-23 21:46:39.520',NULL,'Manajemen OWNER',true,false,true,'AU_OW','Manajemen OWNER','ba756655-3a8f-4074-a328-ee12ecb2fa2b','2022-08-23 21:46:39.520',1),
	 ('2022-08-23 21:46:39.560',NULL,'Manajemen Pengguna',true,false,false,'AU_PENG','Manajemen Pengguna','a0754d41-d90c-4384-9381-e8d63fd09fcf','2022-08-23 21:46:39.560',1),
	 ('2022-08-23 21:46:39.578',NULL,'Manajemen Peran',true,false,false,'AU_PER','Manajemen Peran','a905ada4-39c9-49db-b5bf-672fae86f799','2022-08-23 21:46:39.578',1),
	 ('2022-08-23 21:46:39.595',NULL,'Manajemen Hak Akses',true,false,true,'AU_HA','Manajemen Hak Akses','001f67f8-5ec1-4a7b-b9b4-ce46353296f7','2022-08-23 21:46:39.595',1),
	 ('2022-08-23 21:46:39.612',NULL,'Manajemen Modul',true,false,true,'AU_M','Manajemen Modul','9ede6709-7c18-4d23-9b4f-730fb45a228f','2022-08-23 21:46:39.612',1),
	 ('2022-08-23 21:46:39.646',NULL,'Manajemen Project',true,false,true,'AU_P','Manajemen Project','42498fdb-9499-48a7-b863-a63216b9d5da','2022-08-23 21:46:39.646',1),
	 ('2023-01-01 14:26:21.146',NULL,'Modul untuk Kode Akun',true,false,false,'AK_KA','Kode Akun','6d97524e-de28-4d3f-a603-801ca5e580da','2023-01-01 14:26:21.146',2);

INSERT INTO public.hak_akses (created_at,deleted_at,deskripsi,is_active,is_deleted,kode,nama,public_id,updated_at,modul_id) VALUES
	 ('2022-08-23 21:46:40.502',NULL,'Administrator Permission',true,false,'ADMIN','Administrator Permission','96353be9-1c1b-4ec0-a49a-1b4bee129a6f','2022-08-23 21:46:40.502',1),
	 ('2022-08-23 21:46:40.520',NULL,'Owner Administrator Permission',true,false,'AU_OW','Owner Administrator Permission','ca64320d-5e48-4f88-9bbe-6948cd52ea83','2022-08-23 21:46:40.520',1),
	 ('2022-08-23 21:46:40.535',NULL,'Melihat Pengguna',true,false,'AU_PENG_R','Melihat Pengguna','1716f1f6-842a-4345-8f29-b74d7c082286','2022-08-23 21:46:40.535',2),
	 ('2022-08-23 21:46:40.551',NULL,'Membuat Pengguna',true,false,'AU_PENG_C','Membuat Pengguna','dbc85bff-808d-4a59-8428-ffe58fa8e7cf','2022-08-23 21:46:40.551',2),
	 ('2022-08-23 21:46:40.561',NULL,'Mengubah Pengguna',true,false,'AU_PENG_U','Mengubah Pengguna','8aa57df0-4543-4012-a8d0-9bc4948b6c91','2022-08-23 21:46:40.561',2),
	 ('2022-08-23 21:46:40.574',NULL,'Menghapus Pengguna',true,false,'AU_PENG_D','Menghapus Pengguna','2110e00b-928a-446b-ad59-a5dd01920a4e','2022-08-23 21:46:40.574',2),
	 ('2022-08-23 21:46:40.586',NULL,'Mengubah Peran Penggun',true,false,'AU_PENG_R_U','Mengubah Peran Pengguna','0b25d912-97cf-47eb-abba-bfb742ee3348','2022-08-23 21:46:40.586',2),
	 ('2022-08-23 21:46:40.602',NULL,'Melihat Peran',true,false,'AU_PER_R','Melihat Peran','71f93ee4-bfd7-4706-85ac-7ca9ce486b2d','2022-08-23 21:46:40.602',3),
	 ('2022-08-23 21:46:40.614',NULL,'Membuat Peran',true,false,'AU_PER_C','Membuat Peran','4359fd04-54b4-4c75-8f4d-369db529086d','2022-08-23 21:46:40.614',3),
	 ('2022-08-23 21:46:40.628',NULL,'Mengubah Peran',true,false,'AU_PER_U','Mengubah Peran','9e864ca7-83b7-43c9-a153-fdc642bcfa44','2022-08-23 21:46:40.628',3);
INSERT INTO public.hak_akses (created_at,deleted_at,deskripsi,is_active,is_deleted,kode,nama,public_id,updated_at,modul_id) VALUES
	 ('2022-08-23 21:46:40.640',NULL,'Menghapus Peran',true,false,'AU_PER_D','Menghapus Peran','e18a9bb1-6c7a-4852-8348-21f29ec73322','2022-08-23 21:46:40.640',3),
	 ('2022-08-23 21:46:40.652',NULL,'Melihat Hak Akses',true,false,'AU_HA_R','Melihat Hak Akses','6e039c0b-4239-4c85-8ed1-16b3a648e2b8','2022-08-23 21:46:40.652',4),
	 ('2022-08-23 21:46:40.667',NULL,'Membuat Hak Akses',true,false,'AU_HA_C','Membuat Hak Akses','8fb632a6-d4be-4795-b5c3-76294bb59709','2022-08-23 21:46:40.667',4),
	 ('2022-08-23 21:46:40.678',NULL,'Mengubah Hak Akses',true,false,'AU_HA_U','Mengubah Hak Akses','8730b8c2-c9e2-46f6-9f5e-283c6334c7f3','2022-08-23 21:46:40.678',4),
	 ('2022-08-23 21:46:40.689',NULL,'Menghapus Hak Akses',true,false,'AU_HA_D','Menghapus Hak Akses','6cf76373-0768-498e-9f64-4aead5a89e59','2022-08-23 21:46:40.689',4),
	 ('2022-08-23 21:46:40.702',NULL,'Melihat Modul',true,false,'AU_M_R','Melihat Modul','ab49eb33-c514-456e-a28d-b2b5487889ef','2022-08-23 21:46:40.702',5),
	 ('2022-08-23 21:46:40.720',NULL,'Membuat Modul',true,false,'AU_M_C','Membuat Modul','b1905637-e245-4359-b1ea-0a64416c99c3','2022-08-23 21:46:40.720',5),
	 ('2022-08-23 21:46:40.733',NULL,'Mengubah Modul',true,false,'AU_M_U','Mengubah Modul','0f7b3b83-ffa8-47bd-b204-cee583615d5e','2022-08-23 21:46:40.733',5),
	 ('2022-08-23 21:46:40.745',NULL,'Menghapus Modul',true,false,'AU_M_D','Menghapus Modul','e7401181-971d-4596-9899-38f6192826a5','2022-08-23 21:46:40.745',5),
	 ('2022-08-23 21:46:40.796',NULL,'Melihat Project',true,false,'AU_P_R','Melihat Project','a1ef3241-78b0-4de5-abcf-9a64a9303d58','2022-08-23 21:46:40.796',6);
INSERT INTO public.hak_akses (created_at,deleted_at,deskripsi,is_active,is_deleted,kode,nama,public_id,updated_at,modul_id) VALUES
	 ('2022-08-23 21:46:40.805',NULL,'Membuat Project',true,false,'AU_P_C','Membuat Project','bd63bc88-610f-4c1a-be29-ad33851a41ac','2022-08-23 21:46:40.805',6),
	 ('2022-08-23 21:46:40.818',NULL,'Mengubah Project',true,false,'AU_P_U','Mengubah Project','d053b609-d57a-4989-bb6e-1ee14090a4a8','2022-08-23 21:46:40.818',6),
	 ('2022-08-23 21:46:40.831',NULL,'Menghapus Project',true,false,'AU_P_D','Menghapus Project','33f1e669-54e0-4959-a9fc-b3e00dde1b9f','2022-08-23 21:46:40.831',6),
	 ('2023-01-01 14:26:21.203',NULL,'Hak Akses untuk menghapus Kode Akun',true,false,'AK_KA_D','Mengahapus Kode Akun','a0936012-52ac-4451-b3cb-dca642970634','2023-01-01 14:26:21.203',7),
	 ('2023-01-01 14:26:21.219',NULL,'Hak Akses untuk mengubah Kode Akun',true,false,'AK_KA_U','Mengubah Kode Akun','46bd6efc-eb33-4ead-a2d9-54bf188693f6','2023-01-01 14:26:21.219',7),
	 ('2023-01-01 14:26:21.223',NULL,'Hak Akses untuk membuat Kode Akun',true,false,'AK_KA_C','Membuat Kode Akun','8139051d-93e9-4e23-9faa-48871ab64ac2','2023-01-01 14:26:21.223',7),
	 ('2023-01-01 14:26:21.227',NULL,'Hak Akses untuk melihat Kode Akun',true,false,'AK_KA_R','Melihat Kode Akun','ca1cf39d-99c1-4005-a0b6-2c6a2b176b4d','2023-01-01 14:26:21.227',7);

INSERT INTO public.pengguna (created_at,deleted_at,is_deleted,is_locked,nama,"password",password_kedaluwarsa,public_id,updated_at,username,created_by,deleted_by,updated_by) VALUES
	 ('2022-08-23 21:46:40.818',NULL,false,false,'adminClobasoft','$2a$10$NkXSORTO5p.aXlrdQlXNpOmeB0mLXn5JVryvBDiIRbEfRPcXmb09G','2030-08-23 00:00:00.000','b62bf63c-c651-4f5a-ad58-8cfc7a9e70e5','2022-08-23 21:46:40.818','adminClobasoft',1,NULL,1);

INSERT INTO public.peran (created_at,deleted_at,is_active,is_deleted,deskripsi,kode,nama,public_id,updated_at,created_by,deleted_by,updated_by) VALUES
	 ('2022-08-23 21:46:40.818',NULL,false,false,'Role Super Admin','R_SA','Role Super Admin','1b97283b-ddb8-4551-9224-059c9bc8f691','2022-08-23 21:46:40.818',1,NULL,1),
	 ('2023-01-01 14:55:00.245',NULL,false,false,'Peran untuk Admin Akuntansi','R_AA','Admin Akuntansi','5619a780-ee76-4a82-b07f-8510c4c6a101','2023-01-01 14:59:54.493',NULL,NULL,NULL);

INSERT INTO public.peran_hak_akses (peran_id,hak_akses_id) VALUES
	 (1,1),
	 (1,2),
	 (1,3),
	 (1,4),
	 (1,5),
	 (1,6),
	 (1,7),
	 (1,8),
	 (1,9),
	 (1,10);
INSERT INTO public.peran_hak_akses (peran_id,hak_akses_id) VALUES
	 (1,11),
	 (1,12),
	 (1,13),
	 (1,14),
	 (1,15),
	 (1,16),
	 (1,17),
	 (1,18),
	 (1,19),
	 (1,20);
INSERT INTO public.peran_hak_akses (peran_id,hak_akses_id) VALUES
	 (1,21),
	 (1,22),
	 (1,23),
	 (1,24),
	 (1,25),
	 (1,26),
	 (1,27);
	 
INSERT INTO public.peran_hak_akses (peran_id,hak_akses_id) VALUES
	 (2,24),
	 (2,25),
	 (2,26),
	 (2,27);

INSERT INTO public.pengguna_peran (pengguna_id,peran_id) VALUES
	 (1,1);