package com.busanit501.helloworld.member.dao;

import com.busanit501.helloworld.jdbcex.dao.ConnectionUtil;
import com.busanit501.helloworld.member.vo.MemberVO;
import lombok.Cleanup;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {
    // 화면으로부터 전달받은 ,
    // String mid, String mpw
    // 예시 ) mid: lsy, mpw: 1234
    public MemberVO getMemberWithMpw(String mid, String mpw) throws SQLException {
        String query = "select * from tbl_member where mid=? and mpw=?";
        // 결과 데이터를 담아둘 임시 박스 MemberVO
        MemberVO memberVO = null;

        @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, mid);
        preparedStatement.setString(2, mpw);
        @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        memberVO = MemberVO.builder()
                .mid(resultSet.getString("mid"))
                .mpw(resultSet.getString("mpw"))
                .mname(resultSet.getString("mname"))
                .build();

        return memberVO;
    }

    public void updateUuid(String mid, String uuid) throws SQLException {
        String query = "update tbl_member set uuid=? where mid=?";

        @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, uuid);
        preparedStatement.setString(2, mid);
        preparedStatement.executeUpdate();
    } //

    //1 . insert
    // Member 등록기능, 추가하기.
    // VO(Value Object, 실제 디비 컬럼과 일치함)
    // 서비스 계층에서, VO 넘겨 받은 데이터 중에서, 보여줄 데이터만 따로 분리해서,
    // 전달하는 용도로 사용하는 DTO 입니다.

    public void insert(MemberVO memberVO) throws SQLException {

        String sql = "insert into tbl_member (mid, mpw, mname) " +
                "values (?, ?, ?)";
        @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, memberVO.getMid());
        preparedStatement.setString(1, memberVO.getMpw());
        preparedStatement.setBoolean(3, Boolean.parseBoolean(memberVO.getMname()));
        preparedStatement.executeUpdate();
    } //insert

    //2
    // select , DB에서 전체 조회.
    public List<MemberVO> selectAll() throws SQLException {
        String sql = "select * from tbl_member";
        @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
        @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
        // 넘어온 데이터를 임시로 보관할 리스트 인스턴스 만들고,
        // 반복문 통해서, 넘어온 각행을 리스트에 요소로 하나씩 담기.
        List<MemberVO> list = new ArrayList<>();
        while (resultSet.next()) {
            MemberVO memberVO = MemberVO.builder()
                    .mid(String.valueOf(resultSet.getLong("mid")))
                    .mpw(resultSet.getString("mpw"))
                    .mname(String.valueOf(resultSet.getBoolean("mname")))
                    .build();
            list.add(memberVO);
        }
        return list;
    }

    //3, 하나 조회. 상세보기.
    public MemberVO selectOne(Long mid) throws SQLException {
        String sql = "select * from tbl_member where mid = ?";
        @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setLong(1, mid);
        // 하나만 받아온 상태,
        @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
        // 임시 TotoVO , 인스턴스 만들어서, 한행의 각 컬럼 4개를 담기.
        // 0행에서 -> 1행으로 조회를 해야하는데, 요게 누락됨.
        resultSet.next();
        MemberVO memberVO = MemberVO.builder()
                .mid(String.valueOf(resultSet.getLong("mid")))
                .mpw(resultSet.getString("mpw"))
                .mname(String.valueOf(resultSet.getBoolean("mname")))
                .build();
        return memberVO;
    }

    // 수정.
    // update,
    //  화면에서 낱개로 넘어온 데이터는 DTO 담아서 전달,
    // 서비스 계층에서, DTO -> VO(Value Object) 데이터베이스 직접적인 연관있음.
    // 예시, VO 클래스는 , 테이블과 컬럼과 동일.
    // 예시2) ,DTO 화면( 출력에서 전달하고 싶은 데이터만 골라서 사용할수 있음. )
    // 화면에서 받아옴, 테스트 , 더미 데이터 확인.
    public void updateOne(MemberVO memberVO) throws SQLException {
        String sql = " update tbl_member set mid=?, mpw=?, mname=?" +
                " where mid=?";
        @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
        // 화면에서 넘겨받은 변경할 데이터를 DTO -> VO 변환 후에(서비스에서 할 예정.)
        // VO 에서 꺼내서, 디비로 데이터 전달하는 과정.
        preparedStatement.setString(1, memberVO.getMid());
        preparedStatement.setDate(2, Date.valueOf(memberVO.getMpw()));
        preparedStatement.setBoolean(3, Boolean.parseBoolean(memberVO.getMname()));
        preparedStatement.setLong(4, Long.parseLong(memberVO.getMid()));
        preparedStatement.executeUpdate();

    }

    //삭제,
    // delete,
    public void deleteMember(Long mid) throws SQLException {
        String sql = "delete from tbl_member where mid =?";
        @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setLong(1, mid);
        preparedStatement.executeUpdate();

    }



    /// /////////////////////////////////////////////////////////////////////////
    public String getTime() {
        String now = null;
        // hikariCP 이용해서,
        // 디비 연결하고,
        // sql 전달하고,
        // 결과값 받고,
        // 자원 반납
        // 자원 반납 하는 방법 2가지.
        //1)
        // try catch -> try with resource , 자동으로 자원 반납을 함.
        // autocloseable 인터페이스를 구현한 기능들만, 자동 반납.
        // 2) 애너테이션 이용해서, @cleanup , 이용하면, 간단히 자동 반납.
        try (Connection connection = ConnectionUtil.INSTANCE.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement("select now()");
             ResultSet resultSet = preparedStatement.executeQuery();
        ) {
            resultSet.next();
            now = resultSet.getString(1);

        } catch (Exception e) {
            e.printStackTrace();
        } //catch
        return now;
    } //getTime

    public String getTime2() throws SQLException {
        String now = null;
        // 자동으로 디비의 connection 반납하는 방법2
        // @Cleanup
        @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("select now()");
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        now = resultSet.getString(1);
        return now;
    }


} //class


