import type { RegistrationAddress } from './address';

export interface Tokens {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface Profile {
  role: 'ROLE_CUSTOMER' | 'ROLE_ORGANIZER' | 'ROLE_ADMIN';
  name: string;
  email: string;
}

export interface Registration {
  name: string;
  email: string;
  phone: string;
  birthDate: string;
  cpf: string;
  password: string;
  passwordConfirmation: string;
  address: RegistrationAddress;
}
