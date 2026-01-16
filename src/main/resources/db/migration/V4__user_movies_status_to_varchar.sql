alter table user_movies
alter column status type varchar(20)
    using status::text;
