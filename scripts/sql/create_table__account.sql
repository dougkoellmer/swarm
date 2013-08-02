CREATE TABLE account
(
	id						int(11)													NOT NULL,
	email					varchar(255)											NOT NULL,
	username				varchar(16)												NOT NULL,
	password				binary(32)												NOT NULL,
	salt					binary(32)												NOT NULL,
	role					ENUM('USER', 'ADMIN')									NOT NULL,
	creation_date			timestamp				DEFAULT CURRENT_TIMESTAMP		NOT NULL,
	
	new_password			binary(32)												NULL,
	new_salt				binary(32)												NULL,
	password_change_token	binary(32)												NULL,
	password_change_date	timestamp												NULL,
	
	PRIMARY KEY (id),
	UNIQUE(email),
	UNIQUE(username)
	
) ENGINE=InnoDB DEFAULT CHARSET=utf8;