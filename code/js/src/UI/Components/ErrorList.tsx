import React from 'react';

interface ErrorsProps {
    errors: string[];
}

export default function ErrorList({ errors }: ErrorsProps) {
    return (
        <React.Fragment>
            {errors.map((error, index) => (
                <li key={index} color="error">
                    {error}
                </li>
            ))}
        </React.Fragment>
    );
};

