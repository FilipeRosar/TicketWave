export interface CatalogEvent {
  readonly id?: string;
  readonly classification?: EventClassification;
  readonly title: string;
  readonly category: string;
  readonly date: string;
  readonly location: string;
  readonly price: string;
  readonly image: string;
  readonly description: string;
}

export type EventClassification = 'PROFESSIONAL' | 'SOCIAL';
export type NetworkingFeature = 'MEETING_AGENDA' | 'ROOMS' | 'WALL' | 'GROUPS';

export interface EventSummary {
  id: string;
  title: string;
  category: string;
  city: string;
  startDate: string;
  ticketPrice: number;
  shortDescription: string;
  classification: EventClassification;
}

export interface EventDetails {
  description: string;
  classification: EventClassification;
  networkingFeatures: NetworkingFeature[];
}

export interface EventPage {
  content: EventSummary[];
  last: boolean;
}
