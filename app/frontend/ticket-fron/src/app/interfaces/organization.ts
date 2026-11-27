export type OrganizationType = 'PF' | 'PJ';
export type VerificationStatus = 'PENDING' | 'IN_REVIEW' | 'APPROVED' | 'REJECTED';
export type VerificationReason = 'INVALID_DOCUMENT' | 'DATA_MISMATCH' | 'INSUFFICIENT_EVIDENCE';

export interface VerificationSummary {
  id: string;
  status: VerificationStatus;
  reason: VerificationReason | null;
  documentAvailable: boolean;
}

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
  verificationStatus: VerificationStatus;
  verificationReason: VerificationReason | null;
  documentAvailable: boolean;
}
