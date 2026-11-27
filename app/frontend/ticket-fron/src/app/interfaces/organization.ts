export type OrganizationType = 'PF' | 'PJ';
export type OnboardingPending = 'COMMERCIAL_NAME' | 'CONTACT_EMAIL' | 'FINANCIAL_APPROVAL';

export interface CreateOrganization {
  name: string;
  type: OrganizationType;
  document?: string;
}

export interface CommercialProfile {
  commercialName: string | null;
  contactEmail: string | null;
  description: string | null;
}

export interface OrganizationOnboarding extends CommercialProfile {
  id: string;
  name: string;
  type: OrganizationType;
  financialStatus: 'PENDING' | 'APPROVED';
  pending: OnboardingPending[];
}
