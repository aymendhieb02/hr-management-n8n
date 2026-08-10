export interface JourFerieResponse {
  id: number;
  nom: string;
  date: string;
  description: string | null;
  actif: boolean;
  dateCreation?: string;
  dateModification?: string;
}

export interface JourFerieRequest {
  nom: string;
  date: string;
  description?: string | null;
  actif?: boolean;
}
