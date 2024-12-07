-- Creates a set of parameterized procedures to populate the tables with random data.
-- This data does not follow any business logic, it is just random data to test the database performance.
CREATE OR REPLACE PROCEDURE populate_users(num_users INT)
    LANGUAGE plpgsql
AS
$$
DECLARE
    i            INT;
    random_name  VARCHAR(30);
    random_email VARCHAR(50);
BEGIN
    FOR i IN 1..num_users
        LOOP
            random_name := 'user_' || i;
            random_email := random_name || '@isel.pt';
            INSERT INTO users (name, password, email)
            VALUES (random_name, 'ku2RW9E6zhYw9iasAbRTHg==:sJfSxT_B3s5IhgZltHcf1ZGO21cvOQ1mhEm3EFS-8A8=', random_email);
        END LOOP;
END
$$;

CREATE OR REPLACE PROCEDURE populate_sessions()
    LANGUAGE plpgsql
AS
$$
DECLARE
    u_id BIGINT;
BEGIN
    FOR u_id IN (SELECT id FROM users)
        LOOP
            INSERT INTO session (user_id)
            VALUES (u_id);
        END LOOP;
END
$$;

CREATE OR REPLACE PROCEDURE populate_tokens()
    LANGUAGE plpgsql
AS
$$
DECLARE
    s_id BIGINT;
BEGIN
    FOR s_id IN (SELECT id FROM session)
        LOOP
            INSERT INTO access_token (token, session_id)
            VALUES (gen_random_uuid(), s_id);

            INSERT INTO refresh_token (token, session_id)
            VALUES (gen_random_uuid(), s_id);
        END LOOP;
END $$;

CREATE OR REPLACE PROCEDURE populate_channels(num_channels INT)
    LANGUAGE plpgsql
AS
$$
DECLARE
    i              INT;
    random_channel VARCHAR(30);
    is_public      BOOLEAN;
    owner_id       BIGINT;
    owner_ids      BIGINT[];
BEGIN
    SELECT array_agg(id) INTO owner_ids FROM users;
    FOR i IN 1..num_channels
        LOOP
            random_channel := 'channel_' || i;
            is_public := random() < 0.5;
            owner_id := owner_ids[ceil(random() * array_length(owner_ids, 1))];
            INSERT INTO channel (name, owner, is_public)
            VALUES (random_channel, owner_id, is_public);
            INSERT INTO channel_member (channel_id, user_id, role)
            VALUES (currval('channel_id_seq'), owner_id, 'OWNER');
        END LOOP;
END
$$;

CREATE OR REPLACE PROCEDURE populate_channel_members(num_members INT)
    LANGUAGE plpgsql
AS
$$
DECLARE
    i     INT;
    c_ids BIGINT[];
    u_ids BIGINT[];
    role  VARCHAR(10);
    c_id  BIGINT;
    u_id  BIGINT;
BEGIN
    SELECT array_agg(id) INTO c_ids FROM channel;
    SELECT array_agg(id) INTO u_ids FROM users;

    FOR i IN 1..num_members
        LOOP
            c_id := c_ids[ceil(random() * array_length(c_ids, 1))];
            u_id := u_ids[ceil(random() * array_length(u_ids, 1))];
            role := CASE
                        WHEN random() < 0.1 THEN 'OWNER'
                        WHEN random() < 0.4 THEN 'GUEST'
                        ELSE 'MEMBER'
                END;
            BEGIN
                INSERT INTO channel_member (channel_id, user_id, role)
                VALUES (c_id, u_id, role);
            EXCEPTION
                WHEN unique_violation THEN
            END;
        END LOOP;
END
$$;

CREATE OR REPLACE PROCEDURE populate_messages(num_messages INT)
    LANGUAGE plpgsql
AS
$$
DECLARE
    c_id           BIGINT;
    u_id           BIGINT;
    i              INT;
    random_message TEXT;
    c_ids          BIGINT[];
    u_ids          BIGINT[];
BEGIN
    SELECT array_agg(id) INTO c_ids FROM channel;
    SELECT array_agg(id) INTO u_ids FROM users;

    FOR i IN 1..num_messages
        LOOP
            c_id := c_ids[ceil(random() * array_length(c_ids, 1))];
            u_id := u_ids[ceil(random() * array_length(u_ids, 1))];
            random_message := 'Message number ' || i || ' from user ' || u_id;
            INSERT INTO message (channel_id, user_id, content)
            VALUES (c_id, u_id, random_message);
        END LOOP;
END
$$;

CREATE OR REPLACE PROCEDURE populate_channel_messages(num_messages INT, channel_id BIGINT)
    LANGUAGE plpgsql
AS
$$
DECLARE
    u_id           BIGINT;
    i              INT;
    random_message TEXT;
    u_ids          BIGINT[];
BEGIN
    SELECT array_agg(id) INTO u_ids FROM users;
    FOR i IN 1..num_messages
            LOOP
                u_id := u_ids[ceil(random() * array_length(u_ids, 1))];
    random_message := 'Message number ' || i;
    INSERT INTO message (channel_id, user_id, content, created_at)
    VALUES (channel_id, u_id, random_message, NOW() - (i || ' seconds')::interval);
    END LOOP;
END
$$;

CREATE OR REPLACE PROCEDURE create_invitations(
    num_invitations INT,
    inviter_id BIGINT,
    invitee_id BIGINT
)
    LANGUAGE plpgsql
AS
$$
DECLARE
    i INT;
    rand_channel_id BIGINT;
    channel_ids BIGINT[];
BEGIN
    -- Get all channel IDs
    SELECT array_agg(id) INTO channel_ids FROM channel;

    FOR i IN 1..num_invitations
        LOOP
            -- Select a random channel ID
            rand_channel_id := channel_ids[ceil(random() * array_length(channel_ids, 1))];

            IF NOT EXISTS(
                SELECT 1
                FROM channel_member
                WHERE channel_id = rand_channel_id
                AND user_id = inviter_id
            )
                THEN
                    INSERT INTO channel_member (channel_id, user_id, role)
                    VALUES (rand_channel_id, inviter_id, 'OWNER');
            END IF;

            -- Check if an invitation already exists
            IF NOT EXISTS (
                SELECT 1
                FROM channel_invitation
                WHERE rand_channel_id = channel_id
                  AND inviter = inviter_id
                  AND invitee = invitee_id
            ) AND NOT EXISTS (
                SELECT 1
                FROM channel_member
                WHERE channel_id = rand_channel_id
                  AND user_id = invitee_id
            )
                THEN
                    INSERT INTO channel_invitation (channel_id, inviter, invitee)
                    VALUES (rand_channel_id, inviter_id, invitee_id);
            END IF;
        END LOOP;
END
$$;


CALL populate_users(100000);
CALL populate_sessions();
CALL populate_tokens();
CALL populate_channels(10000);
CALL populate_channel_members(50000);
CALL populate_messages(100000);
CALL populate_channel_messages(1000, 1);
CALL create_invitations(100, 2, 1);