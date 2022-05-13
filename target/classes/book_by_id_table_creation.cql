CREATE TABLE IF NOT EXISTS book_by_id (
  id text PRIMARY KEY,
  book_name text,
  book_description text,
  published_date date,
  cover_ids set<text>,
  author_names set<text>,
  author_id set<text>
);