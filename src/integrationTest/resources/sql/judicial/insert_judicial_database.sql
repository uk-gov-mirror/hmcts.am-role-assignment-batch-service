DELETE FROM booking;

--today's record. Should not be deleted.
INSERT INTO public.booking
(id, user_id, appointment_id, role_id, contract_type_id, base_location_id, region_id, status, begin_time, end_time, created, log)
VALUES(uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), 'role_id',
'contract', 'base location', 'region1', 'LIVE',  now(), now(), now(), '');

--today's record. Should not be deleted.
INSERT INTO public.booking
(id, user_id, appointment_id, role_id, contract_type_id, base_location_id, region_id, status, begin_time, end_time, created, log)
VALUES(uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), 'role_id',
'contract', 'base location', 'region2', 'LIVE',  now(), now(), now(), '');

--record expired yesterday
INSERT INTO public.booking
(id, user_id, appointment_id, role_id, contract_type_id, base_location_id, region_id, status, begin_time, end_time, created, log)
VALUES(uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), 'role_id',
'contract', 'base location', 'region', 'LIVE',  (current_date - 1 ) + '11:00:00'::time , (current_date - 1 ) + '13:00:00'::time, now(), '');

--borderline scenario
INSERT INTO public.booking
(id, user_id, appointment_id, role_id, contract_type_id, base_location_id, region_id, status, begin_time, end_time, created, log)
VALUES(uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), 'role_id',
'contract', 'base location', 'region', 'LIVE',  (current_date - 1 ) + '11:00:00'::time , (current_date - 1 ) + '23:59:59'::time, now(), '');

--7 days old record
INSERT INTO public.booking
(id, user_id, appointment_id, role_id, contract_type_id, base_location_id, region_id, status, begin_time, end_time, created, log)
VALUES(uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), 'role_id',
'contract', 'base location', 'region', 'LIVE',  (current_date - 6 ) + '11:00:00'::time , (current_date - 6 ) + '23:59:59'::time, now(), '');