ALTER TABLE account ADD new_password			binary(32)												NULL;
ALTER TABLE account ADD new_salt				binary(32)												NULL;
ALTER TABLE account ADD password_change_token	binary(32)												NULL;
ALTER TABLE account ADD password_change_date	timestamp												NULL;