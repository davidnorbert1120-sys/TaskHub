import { TaskStatus } from './task-status.model';
import { TaskPriority } from './task-priority.model';

export interface TaskItemModel {
  id: number;
  title: string;
  description: string | null;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string | null;
  assigneeUsername: string | null;
  createdAt: string;
  updatedAt: string;
}
