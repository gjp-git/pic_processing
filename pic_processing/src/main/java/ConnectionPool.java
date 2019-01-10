public interface ConnectionPool<E> {
    //取出连接
    public E checkout(int waitTime);

    //回收连接
    public void checkin(E connection);

    //如果连接无效则抛弃，新建连接来补充到池里
    public void drop(E connection);

    //关闭所有连接
    public void closeAll();
}
