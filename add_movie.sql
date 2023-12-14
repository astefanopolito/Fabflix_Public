Use moviedb;

DELIMITER $$

CREATE PROCEDURE add_movie (
    title varchar(100),
    m_year int,
    director varchar(100),
    star_name varchar(100),
    genre varchar(32)
)

BEGIN
	IF NOT EXISTS (SELECT * FROM stars WHERE name = star_name) THEN
		INSERT INTO stars VALUES (CONCAT("nm", (CAST(substring((select cid from (select max(id) as cid from stars) as c), 3) AS UNSIGNED) + 1)), star_name, NULL);
	END IF;
	IF NOT EXISTS (SELECT * FROM genres WHERE name = genre) THEN
		INSERT INTO genres VALUES ((select cid from (select max(id) as cid from genres) as c) + 1, genre);
	END IF;
    IF EXISTS (SELECT * FROM movies WHERE movies.title = title AND movies.year = m_year AND movies.director = director) THEN
		SELECT CONCAT(title, " already exists") as message;
	ELSE
		INSERT INTO movies (id, title, year, director)  VALUES (CONCAT("tt", (CAST(substring((select cid from (select max(id) as cid from movies) as c), 3) AS UNSIGNED) + 1)), title, m_year, director);
        INSERT INTO stars_in_movies VALUES ((SELECT id FROM stars WHERE name = star_name LIMIT 1), (SELECT id FROM movies WHERE movies.title = title AND movies.year = m_year AND movies.director = director LIMIT 1));
		INSERT INTO genres_in_movies VALUES ((SELECT id FROM genres WHERE name = genre LIMIT 1), (SELECT id FROM movies WHERE movies.title = title AND movies.year = m_year AND movies.director = director LIMIT 1));
        SELECT CONCAT("Successfully added ", title) as message;
	END IF;
END
$$

DELIMITER ;

SHOW PROCEDURE STATUS WHERE db = 'moviedb';
		
    
	
	

	