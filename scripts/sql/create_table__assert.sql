CREATE TABLE bh_telemetry.assert
(
	account_id		int(11)													NOT NULL,
	message			varchar(255)											NOT NULL,
	platform		varchar(255)											NOT NULL,
	ip				varchar(32)												NOT NULL,
	date			timestamp				DEFAULT CURRENT_TIMESTAMP		NOT NULL
	
) ENGINE=InnoDB DEFAULT CHARSET=utf8;