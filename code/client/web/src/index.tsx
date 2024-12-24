import './index.css';
import { createRoot } from 'react-dom/client';
import * as React from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { App } from './App';
import { Home } from './UI/Pages/Home/Home';
import SignIn from './UI/Pages/SignIn/SignIn';
import SignUp from './UI/Pages/SignUp/SignUp';
import { CreateChannelWindow } from './UI/Pages/Home/CreateChannelWindow';
import { ChannelSearchWindow } from './UI/Pages/Home/ChannelSearchWindow';
import { CreateImInvitationWindow } from './UI/Pages/Home/CreateImInvitationWindow';
import { Routes } from './routes';
import { InviteMember } from './UI/Pages/Home/ChannelSettings/Windows/InviteMember';
import { ChannelSettingsWindow } from './UI/Pages/Home/ChannelSettings/ChannelSettingsWindow';
import { DeleteChannel } from './UI/Pages/Home/ChannelSettings/Windows/DeleteChannel';
import { LeaveChannel } from './UI/Pages/Home/ChannelSettings/Windows/LeaveChannel';
import { EditChannel } from './UI/Pages/Home/ChannelSettings/Windows/EditChannel';
import { ChannelMembers } from './UI/Pages/Home/ChannelSettings/Windows/ChannelMembers';
import { ChannelInvitations } from './UI/Pages/Home/ChannelSettings/Windows/ChannelInvitations';

const root = createRoot(document.getElementById('root'));

export const routes = createBrowserRouter([
    {
        path: '/',
        element: <App />,
        children: [
            {
                path: Routes.SIGN_IN,
                element: <SignIn />,
            },
            {
                path: Routes.SIGN_UP,
                element: <SignUp />,
            },
            {
                path: Routes.REFERRAL,
                element: <SignUp />,
            },
            {
                path: Routes.HOME,
                element: <Home />,
                children: [
                    {
                        path: Routes.CREATE_CHANNEL,
                        element: <CreateChannelWindow />,
                    },
                    {
                        path: Routes.CREATE_IM_INVITATION,
                        element: <CreateImInvitationWindow />,
                    },
                    {
                        path: Routes.SEARCH_CHANNELS,
                        element: <ChannelSearchWindow />,
                    },
                    {
                        path: Routes.CHANNEL_SETTINGS,
                        element: <ChannelSettingsWindow />,
                        children: [
                            {
                                path: Routes.INVITE_CHANNEL_MEMBER,
                                element: <InviteMember />,
                            },
                            {
                                path: Routes.DELETE_CHANNEL,
                                element: <DeleteChannel />,
                            },
                            {
                                path: Routes.LEAVE_CHANNEL,
                                element: <LeaveChannel />,
                            },
                            {
                                path: Routes.EDIT_CHANNEL,
                                element: <EditChannel />,
                            },
                            {
                                path: Routes.CHANNEL_MEMBERS,
                                element: <ChannelMembers />,
                            },
                            {
                                path: Routes.CHANNEL_INVITATIONS,
                                element: <ChannelInvitations />,
                            },
                        ],
                    },
                ],
            },
        ],
    },
]);

root.render(<RouterProvider router={routes} future={{ v7_startTransition: true }} />);
