import { User } from '../../../Domain/user/User';
import { VirtualizedList } from '../Utils/VirtualizedList';
import Button from '@mui/material/Button';
import { ListItem } from '@mui/material';
import Typography from '@mui/material/Typography';
import React from 'react';
import { UserView } from './UserView';

export function UserList({
    items,
    loadMore,
    isLoaded,
    onClick,
}: {
    items: User[];
    loadMore?: () => void;
    isLoaded?: boolean;
    onClick?: (user: User) => void;
}) {
    return (
        <div
            style={{
                display: 'flex',
                flexDirection: 'column',
                alignSelf: 'center',
                justifySelf: 'center',
                width: '100%',
                height: '100%',
                marginTop: '1em',
                border: '1px solid',
                borderColor: 'divider',
                borderRadius: '5px',
                backgroundColor: 'background.paper',
            }}
        >
            <VirtualizedList
                items={items}
                fixedHeight={40}
                itemStyle={{
                    width: '90%',
                    height: '40px',
                    marginLeft: 'auto',
                    marginBottom: '0.5em',
                }}
                listStyle={{
                    width: '100%',
                    height: '30vh',
                    paddingBottom: '1em',
                }}
                renderItem={({ index, style }) => {
                    const user = items[index];
                    return onClick ? (
                        <Button
                            key={index}
                            style={{
                                ...style,
                                border: '1px solid',
                                borderColor: 'divider',
                            }}
                            onClick={() => onClick(user)}
                        >
                            <UserView user={user} />
                        </Button>
                    ) : (
                        <ListItem
                            key={user.id.value}
                            sx={{
                                ...style,
                                justifyContent: 'center',
                                alignItems: 'center',
                            }}
                        >
                            <UserView user={user} />
                        </ListItem>
                    );
                }}
                onItemsRendered={(_, end) => {
                    if (end === items.length - 1) {
                        loadMore();
                    }
                }}
                header={
                    isLoaded &&
                    items.length === 0 && (
                        <ListItem>
                            <Typography variant={'body1'} sx={{ textAlign: 'center' }}>
                                No users
                            </Typography>
                        </ListItem>
                    )
                }
            />
        </div>
    );
}
