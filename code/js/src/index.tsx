import './index.css';
import { createRoot } from 'react-dom/client';
import * as React from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { App } from './App';
import { Home } from './UI/Pages/Home/Home';
import SignIn from './UI/Pages/Auth/SignIn/SignIn';
import SignUp from './UI/Pages/Auth/SignUp/SignUp';

const root = createRoot(document.getElementById('root'));

const routes = createBrowserRouter([
    {
        path: '/',
        element: <App />,
        children: [
            {
                path: '/sign-in',
                element: <SignIn />,
            },
            {
                path: '/sign-up',
                element: <SignUp />,
            },
            {
                path: '/home',
                element: <Home />,
                children: [
                    {
                        path: 'create-channel',
                        // TODO <CreateChannelWindow /> should be a pop-up window
                        element: <div>Create Channel</div>,
                    },
                ],
            },
        ],
    },
]);

root.render(
    <RouterProvider router={routes} future={{ v7_startTransition: true }} />,
);