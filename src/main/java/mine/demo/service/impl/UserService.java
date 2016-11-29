package mine.demo.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import mine.demo.dao.IUserDao;
import mine.demo.pojo.User;
import mine.demo.service.IUserService;

@Service("userService")
public class UserService implements IUserService {

	@Resource
	private IUserDao userDao;

	@Override
	public User getUserById(int userId) {
		// TODO Auto-generated method stub
		return this.userDao.selectByPrimaryKey(userId);
	}

}
