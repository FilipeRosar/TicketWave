export interface ParticipantProfile {
  name: string;
  occupation: string | null;
  city: string | null;
  bio: string | null;
  photoType: 'image/jpeg' | 'image/png' | null;
  photo: string | null;
}
