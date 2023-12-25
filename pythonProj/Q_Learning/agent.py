import sys

import numpy as np


class QLearning(object):
    def __init__(self, state_dim, action_dim, cfg):
        self.action_dim = action_dim  # dimension of action
        self.lr = cfg.lr  # learning rate
        self.gamma = cfg.gamma # 衰减系数
        self.epsilon = 0
        self.sample_count = 0
        self.Q_table = np.zeros((state_dim, action_dim))  # Q表格

    def choose_action(self, state):
        ####################### 智能体的决策函数，需要完成Q表格方法（需要完成）#######################
        self.sample_count += 1
        return self.predict(state)

    def predict(self, state):
        q_table = self.Q_table[state]
        action = np.where(q_table == np.max(q_table))  # 选取Q最大的动作
        return np.random.choice(action[0])

    def update(self, state, action, reward, next_state, done):
        ############################ Q表格的更新方法（需要完成）##################################
        if done:
            self.Q_table[state][action] += (self.lr*(reward-self.Q_table[state][action]))
        else:
            self.Q_table[state][action] += (self.lr * (reward + self.gamma*np.max(self.Q_table[next_state]) - self.Q_table[state][action]))

    def save(self, path):
        np.save(path + "Q_table.npy", self.Q_table)

    def load(self, path):
        self.Q_table = np.load(path + "Q_table.npy")
