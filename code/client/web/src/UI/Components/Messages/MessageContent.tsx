import React from 'react';
import Link from '@mui/material/Link';
import { styled } from '@mui/material/styles';
import Typography from '@mui/material/Typography';

const Content = styled(Typography)(({ theme }) => ({
    fontSize: '1rem',
    color: theme.palette.text.primary,
    textAlign: 'left',
    whiteSpace: 'pre-wrap',
    hyphens: 'auto',
    maxWidth: '100%',
    lineHeight: '1.25rem',
    letterSpacing: '0.01rem',
    marginTop: '0.25rem',
    marginBottom: '0.25rem',
    [theme.breakpoints.down('lg')]: {
        fontSize: '0.8rem',
    },
}));

export function MessageContent(props: { content: string }) {
    return (
        <Content>
            {props.content.split('\n').map((line, lineIndex) => (
                <React.Fragment key={lineIndex}>
                    {line.split(/[ ,]+/).map((word, wordIndex) =>
                        word.startsWith('http') ? (
                            <Link key={wordIndex} href={word} target="_blank" rel="noreferrer">
                                {word}
                            </Link>
                        ) : (
                            word + ' '
                        ),
                    )}
                    <br />
                </React.Fragment>
            ))}
        </Content>
    );
}
