package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import db_info.DbInfo;
import db_info.SQLStatment;
import dto.BookingDTO;
import dto.MovieDTO;
import dto.ScreeningScheduleDTO;
import dto.SeatDTO;
import dto.UserDTO;

public class DAO implements DbInfo, SQLStatment {

	final static String DATABASE_URL = "jdbc:mysql://localhost:3306/" + DATABASE + "?serverTimezone=Asia/Seoul";
	private String DbId = USER_ID;
	private String DbPw = USER_PW;

	private DAO() {
	}

	private static DAO dao = new DAO();

	public static DAO getDAO() {
		return dao;
	}

	{
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public UserDTO selectUserByIdAndPw(String id, String pw, int isAdmin) {
		DbId = USER_ID;
		DbPw = USER_PW;
		if (isAdmin == 1) {
			DbId = ROOT_ID;
			DbPw = ROOT_PW;
		}
		UserDTO rsltUserDTO = null;
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement selectUserByIdAndPwStmt = conn.prepareStatement(SELECT_USER_BY_ID_AND_PW_QUERY);) {
			selectUserByIdAndPwStmt.setString(1, id);
			selectUserByIdAndPwStmt.setString(2, pw);
			selectUserByIdAndPwStmt.setInt(3, isAdmin);
			try (ResultSet rs = selectUserByIdAndPwStmt.executeQuery()) {
				if (rs.next()) {
					rsltUserDTO = new UserDTO(rs.getString("user_id"), rs.getString("user_name"),
							rs.getString("phone_no"), rs.getString("email"), rs.getString("password"),
							rs.getInt("is_admin"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rsltUserDTO;
	}

	public List<MovieDTO> selectMoviesWithActorNames(String title, String director, String[] actorArray, String genre) {
		List<MovieDTO> rsltMovies = new LinkedList<MovieDTO>();
		StringBuilder actorNameCondition = new StringBuilder(100);
		for (String actorName : actorArray) {
			actorNameCondition.append(" AND actor_name LIKE '%" + actorName + "%'");
		}
		String selectMoviesWithActorNameQuery = SELECT_MOVIES_WITH_ACTOR_NAME_QUERY
				+ " WHERE movie_name LIKE ? AND director_name LIKE ?" + actorNameCondition.toString()
				+ " AND genre LIKE ?" + " GROUP BY movie_no";
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement selectMoviesStmt = conn.prepareStatement(selectMoviesWithActorNameQuery);) {
			selectMoviesStmt.setString(1, "%" + title + "%");
			selectMoviesStmt.setString(2, "%" + director + "%");
			selectMoviesStmt.setString(3, "%" + genre + "%");
			selectMoviesStmt.setString(1, "%" + title + "%");
			selectMoviesStmt.setString(2, "%" + director + "%");
			selectMoviesStmt.setString(3, "%" + genre + "%");
			try (ResultSet rs = selectMoviesStmt.executeQuery()) {
				while (rs.next()) {
					MovieDTO movieDTO = new MovieDTO(rs.getInt("movie_no"), rs.getString("movie_name"),
							rs.getInt("running_time"), rs.getInt("age_rating"), rs.getString("director_name"),
							rs.getString("genre"), rs.getDate("release_date"), rs.getString("movie_info"),
							rs.getFloat("rating_information"), rs.getString("actor_names"));
					rsltMovies.add(movieDTO);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rsltMovies;
	}

	public List<MovieDTO> selectMovies(String title, String director, String genre) {
		List<MovieDTO> rsltMovies = new LinkedList<MovieDTO>();
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement selectMoviesStmt = conn.prepareStatement(SELECT_MOVIES_QUERY);) {
			selectMoviesStmt.setString(1, "%" + title + "%");
			selectMoviesStmt.setString(2, "%" + director + "%");
			selectMoviesStmt.setString(3, "%" + genre + "%");
			try (ResultSet rs = selectMoviesStmt.executeQuery()) {
				while (rs.next()) {
					MovieDTO movieDTO = new MovieDTO(rs.getInt("movie_no"), rs.getString("movie_name"),
							rs.getInt("running_time"), rs.getInt("age_rating"), rs.getString("director_name"),
							rs.getString("genre"), rs.getDate("release_date"), rs.getString("movie_info"),
							rs.getFloat("rating_information"), rs.getString("actor_names"));
					rsltMovies.add(movieDTO);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rsltMovies;
	}

	public UserDTO selectUserById(String id) {
		UserDTO rsltUserDTO = null;
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement selectUserByIdStmt = conn.prepareStatement(selectUserById);) {
			selectUserByIdStmt.setString(1, id);
			try (ResultSet rs = selectUserByIdStmt.executeQuery()) {
				if (rs.next()) {
					rsltUserDTO = new UserDTO(rs.getString("user_id"), rs.getString("user_name"),
							rs.getString("phone_no"), rs.getString("email"), rs.getString("password"),
							rs.getInt("is_admin"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rsltUserDTO;
	}

	public boolean insertUser(UserDTO user) {
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement insertUserStmt = conn.prepareStatement(insertUser);) {
			insertUserStmt.setString(1, user.getId());
			insertUserStmt.setString(2, user.getUserName());
			insertUserStmt.setString(3, user.getphoneNo());
			insertUserStmt.setString(4, user.getEmail());
			insertUserStmt.setString(5, user.getPassword());
			insertUserStmt.setInt(6, user.isAdmin());
			insertUserStmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean initializeDatabase() {
		String[] sqlStatements = { "SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;",
				"SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;",
				"SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';",
				"DROP TABLE IF EXISTS booking;", "DROP TABLE IF EXISTS movie_ticket;", "DROP TABLE IF EXISTS user;",
				"DROP TABLE IF EXISTS seat;", "DROP TABLE IF EXISTS screening_schedule;",
				"DROP TABLE IF EXISTS screening_hall;", "DROP TABLE IF EXISTS actor;", "DROP TABLE IF EXISTS casting;",
				"DROP TABLE IF EXISTS movie;",
				"CREATE TABLE `db1`.`movie` (" + "`movie_no` INT NOT NULL AUTO_INCREMENT,"
						+ "`movie_name` CHAR(45) NOT NULL," + "`running_time` INT NOT NULL,"
						+ "`age_rating` INT NOT NULL," + "`director_name` CHAR(20) NOT NULL,"
						+ "`Genre` CHAR(45) NOT NULL," + "`release_date` DATE NOT NULL,"
						+ "`movie_info` TEXT(500) NOT NULL," + "`rating_information` DECIMAL(3,1) NOT NULL,"
						+ "PRIMARY KEY (`movie_no`))" + "ENGINE = InnoDB;",
				"CREATE TABLE `db1`.`screening_hall` (" + "`hall_no` INT NOT NULL AUTO_INCREMENT,"
						+ "`standard_price` INT NOT NULL," + "`hall_name` CHAR(20) NOT NULL,"
						+ "PRIMARY KEY (`hall_no`))" + "ENGINE = InnoDB;",
				"CREATE TABLE `db1`.`screening_schedule` (" + "`schedule_no` INT NOT NULL AUTO_INCREMENT,"
						+ "`hall_no` INT NOT NULL," + "`screening_date` DATE NOT NULL,"
						+ "`screening_day` CHAR(3) NOT NULL," + "`screening_session` INT NOT NULL,"
						+ "`screening_start_time` TIME NOT NULL," + "`movie_no` INT NOT NULL,"
						+ "PRIMARY KEY (`schedule_no`, `hall_no`, `movie_no`),"
						+ "INDEX `fk_screening_schedule_screening_hall1_idx` (`hall_no` ASC) VISIBLE,"
						+ "INDEX `fk_screening_schedule_movie1_idx` (`movie_no` ASC) VISIBLE,"
						+ "CONSTRAINT `fk_screening_schedule_screening_hall1`" + "FOREIGN KEY (`hall_no`)"
						+ "REFERENCES `db1`.`screening_hall` (`hall_no`)" + " ON DELETE NO ACTION"
						+ "  ON UPDATE NO ACTION," + "CONSTRAINT `fk_screening_schedule_movie1`"
						+ "FOREIGN KEY (`movie_no`)" + "REFERENCES `db1`.`movie` (`movie_no`)" + " ON DELETE NO ACTION"
						+ " ON UPDATE NO ACTION)" + "ENGINE = InnoDB;",
				"CREATE TABLE `db1`.`seat` (" + "`hall_no` INT NOT NULL," + "`seat_no` CHAR(6) NOT NULL,"
						+ "PRIMARY KEY (`hall_no`, `seat_no`)," + "CONSTRAINT `fk_seat_screening_hall1`"
						+ "FOREIGN KEY (`hall_no`)" + "REFERENCES `db1`.`screening_hall` (`hall_no`)"
						+ " ON DELETE NO ACTION" + " ON UPDATE NO ACTION)" + "ENGINE = InnoDB;",
				"CREATE TABLE `db1`.`user` (" + "`user_id` CHAR(30) NOT NULL," + "`user_name` CHAR(30) NOT NULL,"
						+ "`phone_no` CHAR(11) NOT NULL," + "`email` CHAR(100) NOT NULL,"
						+ "`password` CHAR(30) NOT NULL," + "`is_admin` TINYINT NOT NULL DEFAULT 0,"
						+ "PRIMARY KEY (`user_id`))" + "ENGINE = InnoDB;",
				"CREATE INDEX idx_seat_no ON seat(seat_no);", // booking의 외래키 설정 떄문
				"CREATE TABLE `db1`.`booking` (" + "`booking_no` INT NOT NULL AUTO_INCREMENT,"
						+ "`payment_method` CHAR(20) NOT NULL,"
						+ "`payment_status` CHAR(20) NOT NULL DEFAULT 'pending'," + "`payment_amount` INT NOT NULL,"
						+ "`payment_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," + "`schedule_no` INT NOT NULL,"
						+ "`seat_no` CHAR(3) NOT NULL," + "`user_id` CHAR(30) NOT NULL,"
						+ "PRIMARY KEY (`booking_no`, `schedule_no`, `seat_no`, `user_id`),"
						+ "INDEX `fk_booking_info_screening_schedule1_idx` (`schedule_no` ASC) VISIBLE,"
						+ "INDEX `fk_booking_info_seat1_idx` (`seat_no` ASC) VISIBLE,"
						+ "INDEX `fk_booking_info_user1_idx` (`user_id` ASC) VISIBLE,"
						+ "CONSTRAINT `fk_booking_info_screening_schedule1`" + "FOREIGN KEY (`schedule_no`)"
						+ "REFERENCES `db1`.`screening_schedule` (`schedule_no`)" + " ON DELETE NO ACTION"
						+ " ON UPDATE NO ACTION," + "CONSTRAINT `fk_booking_info_seat1`" + "FOREIGN KEY (`seat_no`)"
						+ "REFERENCES `db1`.`seat` (`seat_no`)" + " ON DELETE NO ACTION" + " ON UPDATE NO ACTION,"
						+ "CONSTRAINT `fk_booking_info_user1`" + "FOREIGN KEY (`user_id`)"
						+ "REFERENCES `db1`.`user` (`user_id`)" + " ON DELETE NO ACTION" + " ON UPDATE NO ACTION)"
						+ "ENGINE = InnoDB;",
				"CREATE TABLE `db1`.`movie_ticket` (" + "`ticket_no` INT NOT NULL AUTO_INCREMENT,"
						+ "`booking_no` INT NOT NULL," + "PRIMARY KEY (`ticket_no`, `booking_no`),"
						+ "INDEX `fk_movie_ticket_booking_info1_idx` (`booking_no` ASC) VISIBLE,"
						+ "CONSTRAINT `fk_movie_ticket_booking_info1`" + "FOREIGN KEY (`booking_no`)"
						+ "REFERENCES `db1`.`booking` (`booking_no`)" + " ON DELETE NO ACTION" + " ON UPDATE NO ACTION)"
						+ "ENGINE = InnoDB;",
				"CREATE TABLE `db1`.`actor` (" + "`actor_no` INT NOT NULL AUTO_INCREMENT,"
						+ "`actor_name` CHAR(20) NOT NULL," + "PRIMARY KEY (`actor_no`))" + "ENGINE = InnoDB;",
				"CREATE TABLE `db1`.`casting` (" + "`actor_no` INT NOT NULL," + "`movie_no` INT NOT NULL,"
						+ "PRIMARY KEY (`actor_no`, `movie_no`),"
						+ "INDEX `fk_appearance_actor_idx` (`actor_no` ASC) VISIBLE,"
						+ "INDEX `fk_appearance_movie1_idx` (`movie_no` ASC) VISIBLE,"
						+ "CONSTRAINT `fk_appearance_actor`" + "FOREIGN KEY (`actor_no`)"
						+ "REFERENCES `db1`.`actor` (`actor_no`)" + " ON DELETE NO ACTION" + " ON UPDATE NO ACTION,"
						+ "CONSTRAINT `fk_appearance_movie1`" + "FOREIGN KEY (`movie_no`)"
						+ "REFERENCES `db1`.`movie` (`movie_no`)" + " ON DELETE NO ACTION" + " ON UPDATE NO ACTION)"
						+ "ENGINE = InnoDB;",
				"SET SQL_MODE=@OLD_SQL_MODE;", "SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;",
				"SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;" };

		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				Statement stmt = conn.createStatement()) {
			for (String sql : sqlStatements) {
				stmt.executeUpdate(sql);
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean executeSQL(String sql) {
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement stmt = conn.prepareStatement(sql);) {
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public String viewTableData(String tableName) {
		StringBuilder result = new StringBuilder();
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + tableName);
				ResultSet rs = stmt.executeQuery()) {

			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();

			for (int i = 1; i <= columnCount; i++) {
				result.append(metaData.getColumnName(i)).append("\t");
			}
			result.append("\n");

			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					result.append(rs.getString(i)).append("\t");
				}
				result.append("\n");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	public boolean insertData(String tableName, String[] columns, String[] values) {
		StringBuilder query = new StringBuilder("INSERT INTO " + tableName + " (");
		for (String column : columns) {
			query.append(column).append(", ");
		}
		query.setLength(query.length() - 2);
		query.append(") VALUES (");
		for (int i = 0; i < values.length; i++) {
			query.append("?, ");
		}
		query.setLength(query.length() - 2);
		query.append(")");

		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement stmt = conn.prepareStatement(query.toString())) {
			for (int i = 0; i < values.length; i++) {
				stmt.setString(i + 1, values[i]);
			}
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<BookingDTO> getBookingByUserId(String userId) {
		List<BookingDTO> bookingList = new LinkedList<>();
		String query = "SELECT b.booking_no, b.payment_method, b.payment_status, b.payment_amount, b.payment_date, "
				+ "b.schedule_no, b.seat_no, b.user_id, m.movie_name, s.screening_date, h.hall_name, "
				+ "s.screening_start_time " + "FROM booking b "
				+ "JOIN screening_schedule s ON b.schedule_no = s.schedule_no "
				+ "JOIN movie m ON s.movie_no = m.movie_no " + "JOIN screening_hall h ON s.hall_no = h.hall_no "
				+ "WHERE b.user_id = ?";

		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, userId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					BookingDTO booking = new BookingDTO(rs.getInt("booking_no"), rs.getString("payment_method"),
							rs.getString("payment_status"), rs.getInt("payment_amount"),
							rs.getTimestamp("payment_date"), rs.getInt("schedule_no"), rs.getString("seat_no"),
							rs.getString("user_id"), rs.getString("movie_name"), rs.getDate("screening_date"),
							rs.getString("hall_name"), rs.getTime("screening_start_time"));
					bookingList.add(booking);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return bookingList;
	}

	public boolean deleteBooking(int bookingNo) {
		String deleteMovieTicketSql = "DELETE FROM movie_ticket WHERE booking_no = ?";
		String deleteBookingSql = "DELETE FROM booking WHERE booking_no = ?";
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement deleteMovieTicketStmt = conn.prepareStatement(deleteMovieTicketSql);
				PreparedStatement deleteBookingStmt = conn.prepareStatement(deleteBookingSql)) {

			conn.setAutoCommit(false);

			deleteMovieTicketStmt.setInt(1, bookingNo);
			deleteMovieTicketStmt.executeUpdate();

			deleteBookingStmt.setInt(1, bookingNo);
			int rowsAffected = deleteBookingStmt.executeUpdate();

			conn.commit();

			return rowsAffected > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<ScreeningScheduleDTO> selectSchedulesByMovieNo(int movieNo) {
		List<ScreeningScheduleDTO> rsltSchedules = new LinkedList<>();
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement selectSchedulesStmt = conn
						.prepareStatement(SELECT_SCREENING_SCHEDUELES_BY_MOVIE_NO);) {
			selectSchedulesStmt.setInt(1, movieNo);
			try (ResultSet rs = selectSchedulesStmt.executeQuery()) {
				while (rs.next()) {
					ScreeningScheduleDTO ScreeningScheduleDTO = new ScreeningScheduleDTO(rs.getInt("schedule_no"),
							rs.getInt("hall_no"), rs.getDate("screening_date"), rs.getString("screening_day"),
							rs.getInt("screening_session"), rs.getTime("screening_start_time"), rs.getInt("movie_no"),
							rs.getInt("standard_price"));
					rsltSchedules.add(ScreeningScheduleDTO);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rsltSchedules;
	}

	public List<SeatDTO> selectUnbookedSeatsBySchedule(int hallNo, int scheduleNo) {
		List<SeatDTO> unbookedSeats = new ArrayList<>(100);
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement selectSeatsStmt = conn.prepareStatement(SELECT_UNBOOKED_SEATS_BY_SCHEDULE_NO);) {
			selectSeatsStmt.setInt(1, hallNo);
			selectSeatsStmt.setInt(2, scheduleNo);
			try (ResultSet rs = selectSeatsStmt.executeQuery()) {
				while (rs.next()) {
					SeatDTO seatDTO = new SeatDTO(hallNo, rs.getString("seat_no"));
					unbookedSeats.add(seatDTO);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return unbookedSeats;
	}

	public List<SeatDTO> selectAllSeatsByHallNo(int hallNo) {
		List<SeatDTO> seats = new ArrayList<>(100);
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement selectSeatsStmt = conn.prepareStatement(SELECT_ALL_SEATS_BY_HALL_NO);) {
			selectSeatsStmt.setInt(1, hallNo);
			try (ResultSet rs = selectSeatsStmt.executeQuery()) {
				while (rs.next()) {
					SeatDTO seatDTO = new SeatDTO(hallNo, rs.getString("seat_no"));
					seats.add(seatDTO);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return seats;
	}

	public int insertBooking(ScreeningScheduleDTO schedule, SeatDTO seat, UserDTO user) {
		int rslt = 0;
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement insertBookingStmt = conn.prepareStatement(INSERT_BOOKING);) {
			insertBookingStmt.setInt(1, schedule.getStandardPrice());
			insertBookingStmt.setInt(2, schedule.getScheduleNo());
			insertBookingStmt.setString(3, seat.getSeatNo());
			insertBookingStmt.setString(4, user.getId());
			rslt = insertBookingStmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rslt;
	}

	public List<BookingDTO> selectUnpaidBookings(ScreeningScheduleDTO schedule, List<SeatDTO> seats, UserDTO user) {
		List<BookingDTO> bookings = new LinkedList<>();
		for (SeatDTO seat : seats) {
			try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
					PreparedStatement selectbookingsStmt = conn.prepareStatement(SELECT_UNPAID_BOOKING);) {
				selectbookingsStmt.setInt(1, schedule.getScheduleNo());
				selectbookingsStmt.setString(2, seat.getSeatNo());
				selectbookingsStmt.setString(3, user.getId());
				try (ResultSet rs = selectbookingsStmt.executeQuery()) {
					while (rs.next()) {
						BookingDTO bookingDTO = new BookingDTO(rs.getInt("booking_no"), rs.getString("payment_method"),
								rs.getString("payment_status"), rs.getInt("payment_amount"),
								rs.getTimestamp("payment_date"), rs.getInt("schedule_no"), rs.getString("seat_no"),
								rs.getString("user_id"));
						bookings.add(bookingDTO);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return bookings;
	}
	
	public void insertTicket(BookingDTO booking) {
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement insertTicketStmt = conn.prepareStatement(INSERT_TICKET);) {
			insertTicketStmt.setInt(1, booking.getBookingNo());
			insertTicketStmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return;
	}
	
	public void deleteTicket(BookingDTO booking) {
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement deleteTicketStmt = conn.prepareStatement(DELETE_TICKET);) {
			deleteTicketStmt.setInt(1, booking.getBookingNo());
			deleteTicketStmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return;
	}

	public boolean updateBookingSchedule(int bookingNo, int newScheduleNo) {
		String sql = "UPDATE booking SET schedule_no = ? WHERE booking_no = ?";
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, newScheduleNo);
			stmt.setInt(2, bookingNo);
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public int getMovieNoByScheduleNo(int scheduleNo) {
		String sql = "SELECT movie_no FROM screening_schedule WHERE schedule_no = ?";
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, scheduleNo);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("movie_no");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public MovieDTO getMovieByNo(int movieNo) {
		String sql = "SELECT * FROM movie WHERE movie_no = ?";
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, movieNo);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return new MovieDTO(rs.getInt("movie_no"), rs.getString("movie_name"), rs.getInt("running_time"),
							rs.getInt("age_rating"), rs.getString("director_name"), rs.getString("Genre"),
							rs.getDate("release_date"), rs.getString("movie_info"), rs.getFloat("rating_information"),
							(List<String>) null);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean updatePaymentInfo(BookingDTO booking) {
		String sql = "UPDATE booking SET payment_method = ?, payment_status = '결제완료', payment_amount = ? WHERE booking_no = ?";
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, booking.getPaymentMethod());
			stmt.setInt(2, booking.getPaymentAmount());
			stmt.setInt(3, booking.getBookingNo());
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isTicketIssued(int bookingNo) {
		boolean rslt = false;
		try (Connection conn = DriverManager.getConnection(DATABASE_URL, DbId, DbPw);
				PreparedStatement selectTicketStmt = conn.prepareStatement(SELECT_TICKET);) {
			selectTicketStmt.setInt(1, bookingNo);
			try (ResultSet rs = selectTicketStmt.executeQuery()) {
				if (rs.next()) {
					rslt = true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rslt;
	}
	

}
