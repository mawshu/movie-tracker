alter table user_movies
alter column watched_at type timestamp
    using watched_at::timestamp;
