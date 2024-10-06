CREATE OR REPLACE FUNCTION prevent_reused_invitation()
    RETURNS TRIGGER AS
$$
BEGIN
    IF NEW.status = 'USED' AND OLD.status = 'USED' THEN
        RAISE EXCEPTION 'Cannot set status to USED if it is already USED';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_reused_invitation_trigger
    BEFORE UPDATE
    ON im_invitation
    FOR EACH ROW
EXECUTE FUNCTION prevent_reused_invitation();

insert into im_invitation (token) values ('00000000-0000-0000-0000-000000000000');
update im_invitation
set status = 'USED'
where token = '00000000-0000-0000-0000-000000000000';