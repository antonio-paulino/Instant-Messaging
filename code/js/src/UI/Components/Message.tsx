import React from 'react';
import { Message } from '../../Domain/messages/Message';
import { styled } from '@mui/material/styles';
import Typography from '@mui/material/Typography';
import { Card } from '@mui/material';
import { Slide } from 'react-awesome-reveal';
import Divider from '@mui/material/Divider';

const StyledMessageBubble = styled(Card)(({ theme }) => ({
    display: 'flex',
    flexDirection: 'column',
    alignSelf: 'flex-start', // Align left by default
    height: 'fit-content',
    overflow: 'visible',
    width: '100%',
    [theme.breakpoints.up('sm')]: {
        width: '30rem',
    },
    margin: '0.5rem',
    borderRadius: '1rem',
    backgroundColor: theme.palette.background.paper,
    boxShadow:
        'hsla(220, 30%, 5%, 0.05) 0px 5px 15px 0px, hsla(220, 25%, 10%, 0.05) 0px 15px 35px -5px',
}));

const Author = styled(Typography)(({ theme }) => ({
    fontSize: '0.875rem',
    fontWeight: 'bold',
    color: theme.palette.primary.main,
    marginBottom: '0.5rem',
}));

const Timestamp = styled(Typography)(({ theme }) => ({
    fontSize: '0.75rem',
    color: theme.palette.text.secondary,
    textAlign: 'right',
    marginTop: '0.5rem',
}));

const Content = styled(Typography)(({ theme }) => ({
    fontSize: '1rem',
    color: theme.palette.text.primary,
}));

const MessageView: React.FC<{
    message: Message;
    isAuthor: boolean;
    dayDivider?: boolean;
}> = ({ message, isAuthor, dayDivider }) => {
    return (
        <>
            <Slide
                direction={isAuthor ? 'right' : 'left'}
                triggerOnce
                style={{ alignSelf: isAuthor ? 'flex-end' : 'flex-start' }}
            >
                <StyledMessageBubble>
                    <Author>{message.author.name.value}</Author>
                    <Content>{message.content}</Content>
                    <Timestamp>
                        {message.editedAt ? 'Edited ' : ''}
                        {message.createdAt.toLocaleTimeString([], {
                            hour: '2-digit',
                            minute: '2-digit',
                        })}
                    </Timestamp>
                </StyledMessageBubble>
            </Slide>
            {dayDivider && (
                <Divider>{message.createdAt.toDateString()}</Divider>
            )}
        </>
    );
};

export default MessageView;
