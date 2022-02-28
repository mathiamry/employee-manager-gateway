import dayjs from 'dayjs';
import { IDepartment } from 'app/shared/model/department.model';

export interface IEmployee {
  id?: number;
  firstName?: string | null;
  lastName?: string | null;
  dateOfHiring?: string | null;
  salary?: number | null;
  department?: IDepartment | null;
}

export const defaultValue: Readonly<IEmployee> = {};
