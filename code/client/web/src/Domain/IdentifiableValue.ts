import { Identifier } from './wrappers/identifier/Identifier';

export type IdentifiableValue<T> = T & { id: Identifier };
