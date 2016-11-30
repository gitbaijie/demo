package mine.demo.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mine.demo.dao.IUserDao;
import mine.demo.pojo.User;
import mine.demo.service.IUserService;

@Service("userService")
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class) 
public class UserService implements IUserService {

	@Resource
	private IUserDao userDao;

	@Override
	public User getUserById(int userId) {
		// TODO Auto-generated method stub
		return this.userDao.selectByPrimaryKey(userId);
	}

	@Override
	public int insertUser(User record) {
		int i = this.userDao.insert(record);
		return i;
	}

}
