export interface Tokens {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface Profile {
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
}
