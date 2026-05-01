import { TaskStatus } from './task-status.model';
import { TaskPriority } from './task-priority.model';

export interface TaskListItemModel {
  id: number;
  title: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string | null;
  assigneeUsername: string | null;
}
