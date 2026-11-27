export interface RegistrationAddress {
  zipCode: string;
  street: string;
  number: string;
  complement: string;
  district: string;
  city: string;
  state: string;
}

export interface ViaCepResponse {
  erro?: boolean | 'true';
  logradouro?: string;
  bairro?: string;
  localidade?: string;
  uf?: string;
}
