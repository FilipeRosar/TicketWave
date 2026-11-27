export interface ParticipantProfile {
  name: string;
  occupation: string | null;
  city: string | null;
  bio: string | null;
  photoType: 'image/jpeg' | 'image/png' | null;
  photo: string | null;
  interests: string[];
  goals: ConnectionGoal[];
}

export const connectionGoals = {
  HIRE: 'Contratar',
  GET_HIRED: 'Ser contratado',
  MENTORSHIP: 'Mentoria',
  PARTNERSHIP: 'Parceria',
  MEET_PEOPLE: 'Conhecer gente',
  MAKE_FRIENDS: 'Fazer amigos',
} as const;

export type ConnectionGoal = keyof typeof connectionGoals;

export interface ConnectionSuggestion {
  userId: string;
  name: string;
  occupation: string | null;
  city: string | null;
  sharedInterests: string[];
  matchedGoals: ConnectionGoal[];
}
