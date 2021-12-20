DELETE FROM booking;

--today's record. Should not be deleted.
INSERT INTO public.booking
(id, user_id, location_id, region_id, begin_time, end_time, created)
VALUES(uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring),
'base location', 'region1', now(), now(), now());

--today's record. Should not be deleted.
INSERT INTO public.booking
(id, user_id, location_id, region_id, begin_time, end_time, created)
VALUES(uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring),
'base location', 'region2', now(), now(), now());

--record expired yesterday
INSERT INTO public.booking
(id, user_id, location_id, region_id, begin_time, end_time, created)
VALUES(uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring),
'base location', 'region', (current_date - 1 ) + '11:00:00'::time , (current_date - 1 ) + '13:00:00'::time, now());

--borderline scenario
INSERT INTO public.booking
(id, user_id, location_id, region_id, begin_time, end_time, created)
VALUES(uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring),
'base location', 'region', (current_date - 1 ) + '11:00:00'::time , (current_date - 1 ) + '23:59:59'::time, now());

--7 days old record
INSERT INTO public.booking
(id, user_id, location_id, region_id, begin_time, end_time, created)
VALUES(uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring),
'base location', 'region', (current_date - 6 ) + '11:00:00'::time , (current_date - 6 ) + '23:59:59'::time, now());