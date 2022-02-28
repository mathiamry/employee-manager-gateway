export interface IDepartment {
  id?: number;
  name?: string | null;
  address?: string | null;
}

export const defaultValue: Readonly<IDepartment> = {};
