import React, { useEffect, useRef, useState } from 'react';
import logo from './logo.svg';
import './App.css';
import Todo from './components/Todo';
import Form from './components/Form';
import FilterButton from './components/FilterButton';
import { nanoid } from "nanoid";
import usePrevious from './components/usePrevious';

interface Props {
  tasks: Task[]
}

interface Task {
  id: string,
  name: string,
  completed: boolean
}

const filters: { [name: string]: (t: Task) => boolean } = {
  All: (t: Task) => true,
  Active: (t: Task) => !t.completed,
  Completed: (t: Task) => t.completed,
}



function App(props: Props) {
  function addTask(name: string) {
    // const newTask = { id: `todo-${tasks.length+1}`, name, completed: false };

    const newTask = { id: `todo-${nanoid()}`, name, completed: false };

    setTasks([...tasks, newTask]);
  }
  function toggleTaskCompleted(id: string) {

    const updatedTasks = tasks.map((task) => {
      // if this task has the same ID as the edited task
      if (id === task.id) {
        // use object spread to make a new object
        // whose `completed` prop has been inverted
        return { ...task, completed: !task.completed };
      }
      return task;
    });

    setTasks(updatedTasks);
  }

  function deleteTask(id: string) {
    const remainingTasks = tasks.filter((task) => id !== task.id);
    setTasks(remainingTasks);
  }

  function editTask(id: string, name: string) {
    const editedTaskList = tasks.map(t => {
      if (t.id === id) {
        t.name = name;
      }
      return t;
    })

    setTasks(editedTaskList)
  }

  function onClickFilter(filterName: string) {
    
    setCurrentFilter(filterName);

  }

  const [tasks, setTasks] = useState(props.tasks)

  const [currentFilter, setCurrentFilter] = useState("All")

  const filterFunc = filters[currentFilter];

  const filterButtons = Object.keys(filters).map(k => {
    return <FilterButton key={k} buttonName={k} onClick={() => onClickFilter(k)} isPressed={currentFilter === k} />
  });
  
  const taskList = tasks.filter(filterFunc).map((task) => {

    return (<Todo
      id={task.id}
      name={task.name} completed={task.completed}
      key={task.id}
      toggleTaskCompleted={toggleTaskCompleted}
      onDelete={deleteTask}
      editTask={editTask}
    />
    )
  });

  const listHeadingRef = useRef<HTMLHeadingElement>(null);
  const prevTaskLength = usePrevious<number>(tasks.length);
  useEffect(() => {
    if (prevTaskLength && tasks.length - prevTaskLength === -1) {
      listHeadingRef.current?.focus();
    }
  }, [tasks.length, prevTaskLength]);
  
  const tasksNoun = taskList.length !== 1 ? "tasks" : "task";
  const headingText = `${taskList.length} ${tasksNoun} remaining`;

  return (
    <div className='todoapp stack-large'>
      <h1>TodoMatic</h1>

      <Form addTask={addTask} />

      <div className="filters btn-group stack-exception">
        {filterButtons}
      </div>


      <h2 id="list-heading" ref={listHeadingRef}>
        {headingText}
      </h2>
      <ul role="list" className='todo-list stack-large stack-exception' aria-labelledby='list-heading'>
        {taskList}
      </ul>

    </div>
  );
}


export default App;
