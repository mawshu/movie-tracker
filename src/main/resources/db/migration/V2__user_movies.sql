create table if not exists user_movies (
                                           id bigserial primary key,
                                           user_id bigint not null references users(id) on delete cascade,
    movie_id bigint not null references movies(id) on delete cascade,

    status varchar(20) not null,
    rating int,
    liked boolean not null default false,

    watched_at timestamp,
    created_at timestamp not null,
    updated_at timestamp not null,

    constraint uq_user_movie unique (user_id, movie_id)
    );

create index if not exists idx_user_movies_user_id on user_movies(user_id);
create index if not exists idx_user_movies_movie_id on user_movies(movie_id);
